package com.berlin.aetherflow.wms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.common.utils.CodeGenerate;
import com.berlin.aetherflow.common.utils.MapstructUtils;
import com.berlin.aetherflow.common.utils.OrderUtil;
import com.berlin.aetherflow.wms.constant.BizCodeTypeConst;
import com.berlin.aetherflow.wms.constant.OrderStatusConst;
import com.berlin.aetherflow.wms.constant.StockBizTypeConst;
import com.berlin.aetherflow.wms.domain.bo.StockChangeBo;
import com.berlin.aetherflow.wms.domain.bo.TransferOrderActionBo;
import com.berlin.aetherflow.wms.domain.bo.TransferOrderBo;
import com.berlin.aetherflow.wms.domain.bo.TransferOrderItemBo;
import com.berlin.aetherflow.wms.domain.entity.Location;
import com.berlin.aetherflow.wms.domain.entity.Material;
import com.berlin.aetherflow.wms.domain.entity.TransferOrder;
import com.berlin.aetherflow.wms.domain.entity.TransferOrderItem;
import com.berlin.aetherflow.wms.domain.entity.Warehouse;
import com.berlin.aetherflow.wms.domain.query.TransferOrderQuery;
import com.berlin.aetherflow.wms.domain.vo.TransferOrderDetailVo;
import com.berlin.aetherflow.wms.domain.vo.TransferOrderItemVo;
import com.berlin.aetherflow.wms.domain.vo.TransferOrderVo;
import com.berlin.aetherflow.wms.mapper.LocationMapper;
import com.berlin.aetherflow.wms.mapper.MaterialMapper;
import com.berlin.aetherflow.wms.mapper.TransferOrderMapper;
import com.berlin.aetherflow.wms.mapper.WarehouseMapper;
import com.berlin.aetherflow.wms.service.InventoryService;
import com.berlin.aetherflow.wms.service.TransferOrderItemService;
import com.berlin.aetherflow.wms.service.TransferOrderService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 移库单 Service 实现。
 */
@Service
@AllArgsConstructor
public class TransferOrderServiceImpl extends ServiceImpl<TransferOrderMapper, TransferOrder>
        implements TransferOrderService {

    private final TransferOrderMapper transferOrderMapper;
    private final TransferOrderItemService transferOrderItemService;
    private final InventoryService inventoryService;
    private final WarehouseMapper warehouseMapper;
    private final LocationMapper locationMapper;
    private final MaterialMapper materialMapper;

    @Override
    public PageResult<TransferOrderVo> queryList(TransferOrderQuery query) {
        IPage<TransferOrder> page = new Page<>(query.getPageNo(), query.getPageSize());
        OrderUtil.addOrder(page, query.getSortBy(), query.getIsAsc());

        LambdaQueryWrapper<TransferOrder> lqw = Wrappers.<TransferOrder>lambdaQuery()
                .like(StringUtils.isNotBlank(query.getOrderNo()), TransferOrder::getOrderNo, query.getOrderNo())
                .eq(query.getWarehouseId() != null, TransferOrder::getWarehouseId, query.getWarehouseId())
                .eq(query.getStatus() != null, TransferOrder::getStatus, query.getStatus())
                .ge(query.getTransferStartTime() != null, TransferOrder::getTransferTime, query.getTransferStartTime())
                .le(query.getTransferEndTime() != null, TransferOrder::getTransferTime, query.getTransferEndTime())
                .like(StringUtils.isNotBlank(query.getTransferReason()), TransferOrder::getTransferReason, query.getTransferReason())
                .like(StringUtils.isNotBlank(query.getRemark()), TransferOrder::getRemark, query.getRemark());

        IPage<TransferOrder> result = transferOrderMapper.selectPage(page, lqw);
        List<TransferOrderVo> records = result.getRecords().stream()
                .map(item -> MapstructUtils.convert(item, TransferOrderVo.class))
                .toList();
        fillHeaderDisplay(records);
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    @Override
    public TransferOrderDetailVo getDetailById(Long id) {
        TransferOrder order = getById(id);
        if (order == null) {
            return null;
        }

        TransferOrderDetailVo detailVo = MapstructUtils.convert(order, TransferOrderDetailVo.class);
        if (detailVo == null) {
            return null;
        }

        fillHeaderDisplay(List.of(detailVo));
        List<TransferOrderItem> items = transferOrderItemService.lambdaQuery()
                .eq(TransferOrderItem::getOrderId, id)
                .orderByAsc(TransferOrderItem::getLineNo)
                .list();
        List<TransferOrderItemVo> itemVos = items.stream()
                .map(item -> MapstructUtils.convert(item, TransferOrderItemVo.class))
                .toList();
        fillItemDisplay(itemVos);
        detailVo.setOrderItems(itemVos);
        return detailVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createTransferOrder(TransferOrderBo bo) {
        validateWarehouse(bo.getWarehouseId());

        bo.setStatus(OrderStatusConst.DRAFT);
        bo.setTransferTime(null);
        bo.setOrderNo(CodeGenerate.generateSimple(BizCodeTypeConst.TRANSFER_ORDER));
        TransferOrder order = MapstructUtils.convert(bo, TransferOrder.class);
        transferOrderMapper.insert(order);

        List<TransferOrderItemBo> itemsBo = normalizeOrderItems(order.getId(), bo.getOrderItemsBo());
        validateTransferItems(order.getWarehouseId(), itemsBo);
        transferOrderItemService.saveTransferOrderItems(itemsBo);
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateTransferOrder(TransferOrderBo bo) {
        TransferOrder order = getById(bo.getId());
        if (order == null) {
            throw new RuntimeException("移库单不存在");
        }
        if (OrderStatusConst.CONFIRMED.equals(order.getStatus())) {
            throw new RuntimeException("已确认单据不允许编辑");
        }

        Long warehouseIdForValidation = bo.getWarehouseId() != null ? bo.getWarehouseId() : order.getWarehouseId();
        validateWarehouse(warehouseIdForValidation);

        TransferOrder toUpdate = MapstructUtils.convert(bo, TransferOrder.class);
        toUpdate.setStatus(null);
        toUpdate.setTransferTime(null);
        boolean updated = updateById(toUpdate);
        if (!updated) {
            throw new RuntimeException("移库单更新失败");
        }

        if (bo.getOrderItemsBo() != null) {
            List<TransferOrderItemBo> itemsBo = normalizeOrderItems(bo.getId(), bo.getOrderItemsBo());
            validateTransferItems(warehouseIdForValidation, itemsBo);
            transferOrderItemService.replaceTransferOrderItems(bo.getId(), itemsBo);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean applyAction(Long id, TransferOrderActionBo bo) {
        String action = bo.getAction();
        if (StringUtils.isBlank(action)) {
            throw new RuntimeException("动作不能为空");
        }
        action = action.trim().toUpperCase(Locale.ROOT);

        if ("CONFIRM".equals(action)) {
            return confirmTransferOrder(id);
        }

        throw new RuntimeException("不支持的动作: " + action);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeTransferOrders(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }

        List<TransferOrder> orders = lambdaQuery()
                .in(TransferOrder::getId, ids)
                .list();
        if (orders.isEmpty()) {
            return true;
        }

        TransferOrder confirmedOrder = orders.stream()
                .filter(order -> OrderStatusConst.CONFIRMED.equals(order.getStatus()))
                .findFirst()
                .orElse(null);
        if (confirmedOrder != null) {
            throw new RuntimeException("已确认移库单不允许删除: " + confirmedOrder.getOrderNo());
        }

        List<Long> orderIds = orders.stream()
                .map(TransferOrder::getId)
                .toList();
        transferOrderItemService.remove(Wrappers.<TransferOrderItem>lambdaQuery()
                .in(TransferOrderItem::getOrderId, orderIds));
        return removeByIds(orderIds);
    }

    private Boolean confirmTransferOrder(Long id) {
        TransferOrder order = getById(id);
        if (order == null) {
            throw new RuntimeException("移库单不存在");
        }
        if (!OrderStatusConst.DRAFT.equals(order.getStatus())) {
            if (OrderStatusConst.CONFIRMED.equals(order.getStatus())) {
                throw new RuntimeException("移库单已确认，请勿重复提交");
            }
            throw new RuntimeException("当前状态不可确认");
        }

        LocalDateTime confirmTime = LocalDateTime.now();
        int updatedRows = transferOrderMapper.confirmDraftOrder(
                id,
                OrderStatusConst.DRAFT,
                OrderStatusConst.CONFIRMED,
                confirmTime,
                resolveOperator()
        );
        if (updatedRows != 1) {
            TransferOrder latestOrder = getById(id);
            if (latestOrder != null && OrderStatusConst.CONFIRMED.equals(latestOrder.getStatus())) {
                throw new RuntimeException("移库单已确认，请勿重复提交");
            }
            throw new RuntimeException("当前状态不可确认");
        }

        List<TransferOrderItem> items = transferOrderItemService.lambdaQuery()
                .eq(TransferOrderItem::getOrderId, id)
                .orderByAsc(TransferOrderItem::getLineNo)
                .list();
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("移库单明细不能为空");
        }

        List<StockChangeBo> stockChanges = buildStockChanges(order, items, confirmTime);
        inventoryService.applyStockChanges(stockChanges);
        return true;
    }

    private List<TransferOrderItemBo> normalizeOrderItems(Long orderId, List<TransferOrderItemBo> itemsBo) {
        if (itemsBo == null || itemsBo.isEmpty()) {
            throw new RuntimeException("移库单明细不能为空");
        }

        List<TransferOrderItemBo> normalizedItems = new ArrayList<>(itemsBo.size());
        for (int i = 0; i < itemsBo.size(); i++) {
            TransferOrderItemBo item = itemsBo.get(i);
            if (item == null) {
                continue;
            }
            item.setOrderId(orderId);
            if (item.getLineNo() == null) {
                item.setLineNo(i + 1);
            }
            if (Objects.isNull(item.getMaterialId())) {
                throw new RuntimeException("移库单明细物料不能为空");
            }
            if (Objects.isNull(item.getSourceLocationId())) {
                throw new RuntimeException("移库单明细源库位不能为空");
            }
            if (Objects.isNull(item.getTargetLocationId())) {
                throw new RuntimeException("移库单明细目标库位不能为空");
            }
            if (Objects.isNull(item.getTransferQty())) {
                throw new RuntimeException("移库单明细数量不能为空");
            }
            if (item.getTransferQty().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("移库单明细数量必须大于0，行号: " + item.getLineNo());
            }
            item.setBatchNo(normalizeBatchNo(item.getBatchNo()));
            validateBatchDates(item.getProductionDate(), item.getExpiryDate(), item.getLineNo());
            normalizedItems.add(item);
        }
        if (normalizedItems.isEmpty()) {
            throw new RuntimeException("移库单明细不能为空");
        }
        return normalizedItems;
    }

    private void validateWarehouse(Long warehouseId) {
        if (warehouseId == null) {
            throw new RuntimeException("仓库不能为空");
        }
        if (warehouseMapper.selectById(warehouseId) == null) {
            throw new RuntimeException("仓库不存在: " + warehouseId);
        }
    }

    private void validateTransferItems(Long warehouseId, List<TransferOrderItemBo> items) {
        if (warehouseId == null || items == null || items.isEmpty()) {
            return;
        }
        Set<Long> materialIds = items.stream()
                .map(TransferOrderItemBo::getMaterialId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> locationIds = items.stream()
                .flatMap(item -> java.util.stream.Stream.of(item.getSourceLocationId(), item.getTargetLocationId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (materialIds.isEmpty() || locationIds.isEmpty()) {
            return;
        }

        Map<Long, Material> materialMap = materialMapper.selectByIds(materialIds).stream()
                .collect(Collectors.toMap(Material::getId, material -> material, (left, right) -> left));
        Map<Long, Location> locationMap = locationMapper.selectByIds(locationIds).stream()
                .collect(Collectors.toMap(Location::getId, location -> location, (left, right) -> left));
        for (Long materialId : materialIds) {
            if (!materialMap.containsKey(materialId)) {
                throw new RuntimeException("移库单明细存在无效物料: " + materialId);
            }
        }
        for (TransferOrderItemBo item : items) {
            Location sourceLocation = locationMap.get(item.getSourceLocationId());
            if (sourceLocation == null) {
                throw new RuntimeException("移库单明细存在无效源库位: " + item.getSourceLocationId());
            }
            if (!Objects.equals(sourceLocation.getWarehouseId(), warehouseId)) {
                throw new RuntimeException("移库单明细源库位不属于当前仓库: " + sourceLocation.getLocationCode());
            }

            Location targetLocation = locationMap.get(item.getTargetLocationId());
            if (targetLocation == null) {
                throw new RuntimeException("移库单明细存在无效目标库位: " + item.getTargetLocationId());
            }
            if (!Objects.equals(targetLocation.getWarehouseId(), warehouseId)) {
                throw new RuntimeException("移库单明细目标库位不属于当前仓库: " + targetLocation.getLocationCode());
            }
            if (Objects.equals(item.getSourceLocationId(), item.getTargetLocationId())) {
                throw new RuntimeException("移库单明细源库位与目标库位不能相同，行号: " + item.getLineNo());
            }
        }
    }

    private List<StockChangeBo> buildStockChanges(TransferOrder order, List<TransferOrderItem> items,
                                                  LocalDateTime operateTime) {
        List<StockChangeBo> changes = new ArrayList<>(items.size() * 2);
        for (TransferOrderItem item : items) {
            BigDecimal transferQty = item.getTransferQty();

            StockChangeBo outboundChange = new StockChangeBo();
            outboundChange.setBizType(StockBizTypeConst.TRANSFER_ORDER);
            outboundChange.setBizId(order.getId());
            outboundChange.setWarehouseId(order.getWarehouseId());
            outboundChange.setLocationId(item.getSourceLocationId());
            outboundChange.setMaterialId(item.getMaterialId());
            outboundChange.setBatchNo(normalizeBatchNo(item.getBatchNo()));
            outboundChange.setProductionDate(item.getProductionDate());
            outboundChange.setExpiryDate(item.getExpiryDate());
            outboundChange.setLineNo(item.getLineNo());
            outboundChange.setChangeQty(transferQty.negate());
            outboundChange.setOperateTime(operateTime);
            outboundChange.setRemark(buildTransactionRemark(order, item, true));
            changes.add(outboundChange);

            StockChangeBo inboundChange = new StockChangeBo();
            inboundChange.setBizType(StockBizTypeConst.TRANSFER_ORDER);
            inboundChange.setBizId(order.getId());
            inboundChange.setWarehouseId(order.getWarehouseId());
            inboundChange.setLocationId(item.getTargetLocationId());
            inboundChange.setMaterialId(item.getMaterialId());
            inboundChange.setBatchNo(normalizeBatchNo(item.getBatchNo()));
            inboundChange.setProductionDate(item.getProductionDate());
            inboundChange.setExpiryDate(item.getExpiryDate());
            inboundChange.setLineNo(item.getLineNo());
            inboundChange.setChangeQty(transferQty);
            inboundChange.setOperateTime(operateTime);
            inboundChange.setRemark(buildTransactionRemark(order, item, false));
            changes.add(inboundChange);
        }
        return changes;
    }

    private void validateBatchDates(java.time.LocalDate productionDate, java.time.LocalDate expiryDate, Integer lineNo) {
        if (productionDate != null && expiryDate != null && expiryDate.isBefore(productionDate)) {
            throw new RuntimeException("移库单明细到期日期不能早于生产日期，行号: " + lineNo);
        }
    }

    private String normalizeBatchNo(String batchNo) {
        return StringUtils.defaultString(StringUtils.trimToNull(batchNo));
    }

    private String buildTransactionRemark(TransferOrder order, TransferOrderItem item, boolean outbound) {
        List<String> parts = new ArrayList<>();
        parts.add(outbound ? "移出" : "移入");
        if (StringUtils.isNotBlank(order.getTransferReason())) {
            parts.add("原因: " + order.getTransferReason().trim());
        }
        if (StringUtils.isNotBlank(item.getRemark())) {
            parts.add("明细备注: " + item.getRemark().trim());
        } else if (StringUtils.isNotBlank(order.getRemark())) {
            parts.add("单据备注: " + order.getRemark().trim());
        }
        return String.join("；", parts);
    }

    private void fillHeaderDisplay(List<? extends TransferOrderVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        Set<Long> warehouseIds = records.stream()
                .map(TransferOrderVo::getWarehouseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Warehouse> warehouseMap = warehouseIds.isEmpty()
                ? Map.of()
                : warehouseMapper.selectByIds(warehouseIds).stream()
                .collect(Collectors.toMap(Warehouse::getId, warehouse -> warehouse, (left, right) -> left));
        for (TransferOrderVo record : records) {
            Warehouse warehouse = warehouseMap.get(record.getWarehouseId());
            if (warehouse != null) {
                record.setWarehouseCode(warehouse.getWarehouseCode());
                record.setWarehouseName(warehouse.getWarehouseName());
            }
        }
    }

    private void fillItemDisplay(List<TransferOrderItemVo> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<Long> materialIds = items.stream()
                .map(TransferOrderItemVo::getMaterialId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> locationIds = items.stream()
                .flatMap(item -> java.util.stream.Stream.of(item.getSourceLocationId(), item.getTargetLocationId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Material> materialMap = materialIds.isEmpty()
                ? Map.of()
                : materialMapper.selectByIds(materialIds).stream()
                .collect(Collectors.toMap(Material::getId, material -> material, (left, right) -> left));
        Map<Long, Location> locationMap = locationIds.isEmpty()
                ? Map.of()
                : locationMapper.selectByIds(locationIds).stream()
                .collect(Collectors.toMap(Location::getId, location -> location, (left, right) -> left));

        for (TransferOrderItemVo item : items) {
            Material material = materialMap.get(item.getMaterialId());
            if (material != null) {
                item.setMaterialCode(material.getMaterialCode());
                item.setMaterialName(material.getMaterialName());
            }

            Location sourceLocation = locationMap.get(item.getSourceLocationId());
            if (sourceLocation != null) {
                item.setSourceLocationCode(sourceLocation.getLocationCode());
                item.setSourceLocationName(sourceLocation.getLocationName());
            }

            Location targetLocation = locationMap.get(item.getTargetLocationId());
            if (targetLocation != null) {
                item.setTargetLocationCode(targetLocation.getLocationCode());
                item.setTargetLocationName(targetLocation.getLocationName());
            }
        }
    }

    private String resolveOperator() {
        try {
            Object loginId = StpUtil.getLoginIdDefaultNull();
            if (loginId != null) {
                Object operatorName = StpUtil.getTokenSession().get("operatorName");
                if (operatorName != null && StringUtils.isNotBlank(String.valueOf(operatorName))) {
                    return String.valueOf(operatorName);
                }
                return String.valueOf(loginId);
            }
        } catch (Exception ex) {
            return "system";
        }
        return "system";
    }
}
