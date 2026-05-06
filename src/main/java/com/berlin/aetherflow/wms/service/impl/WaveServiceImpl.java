package com.berlin.aetherflow.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.common.utils.CodeGenerate;
import com.berlin.aetherflow.common.utils.MapstructUtils;
import com.berlin.aetherflow.common.utils.OrderUtil;
import com.berlin.aetherflow.wms.constant.WaveConstants;
import com.berlin.aetherflow.wms.constant.PickingTaskStatusConst;
import com.berlin.aetherflow.wms.domain.bo.WaveActionBo;
import com.berlin.aetherflow.wms.domain.bo.WaveBo;
import com.berlin.aetherflow.wms.domain.entity.Area;
import com.berlin.aetherflow.wms.domain.entity.Location;
import com.berlin.aetherflow.wms.domain.entity.Material;
import com.berlin.aetherflow.wms.domain.entity.OutboundOrder;
import com.berlin.aetherflow.wms.domain.entity.OutboundOrderItem;
import com.berlin.aetherflow.wms.domain.entity.PickingTask;
import com.berlin.aetherflow.wms.domain.entity.Warehouse;
import com.berlin.aetherflow.wms.domain.entity.WaveOrder;
import com.berlin.aetherflow.wms.domain.entity.WaveOrderItem;
import com.berlin.aetherflow.wms.domain.query.WaveQuery;
import com.berlin.aetherflow.wms.domain.vo.WaveDetailVo;
import com.berlin.aetherflow.wms.domain.vo.WaveOrderItemVo;
import com.berlin.aetherflow.wms.domain.vo.WaveVo;
import com.berlin.aetherflow.wms.mapper.AreaMapper;
import com.berlin.aetherflow.wms.mapper.LocationMapper;
import com.berlin.aetherflow.wms.mapper.MaterialMapper;
import com.berlin.aetherflow.wms.mapper.OutboundOrderItemMapper;
import com.berlin.aetherflow.wms.mapper.OutboundOrderMapper;
import com.berlin.aetherflow.wms.mapper.WarehouseMapper;
import com.berlin.aetherflow.wms.mapper.WaveOrderItemMapper;
import com.berlin.aetherflow.wms.mapper.WaveOrderMapper;
import com.berlin.aetherflow.wms.service.PickingTaskService;
import com.berlin.aetherflow.wms.service.WaveService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 波次规划 Service 实现。
 */
@Service
@AllArgsConstructor
public class WaveServiceImpl extends ServiceImpl<WaveOrderMapper, WaveOrder> implements WaveService {

    private final WaveOrderMapper waveOrderMapper;
    private final WaveOrderItemMapper waveOrderItemMapper;
    private final OutboundOrderMapper outboundOrderMapper;
    private final OutboundOrderItemMapper outboundOrderItemMapper;
    private final WarehouseMapper warehouseMapper;
    private final AreaMapper areaMapper;
    private final LocationMapper locationMapper;
    private final MaterialMapper materialMapper;
    private final PickingTaskService pickingTaskService;

    @Override
    public PageResult<WaveVo> queryList(WaveQuery query) {
        LambdaQueryWrapper<WaveOrder> lqw = Wrappers.<WaveOrder>lambdaQuery()
                .like(StringUtils.isNotBlank(query.getWaveNo()), WaveOrder::getWaveNo, query.getWaveNo())
                .eq(query.getWarehouseId() != null, WaveOrder::getWarehouseId, query.getWarehouseId())
                .eq(StringUtils.isNotBlank(query.getStatus()), WaveOrder::getStatus, normalizeStatus(query.getStatus()))
                .eq(StringUtils.isNotBlank(query.getGroupRule()), WaveOrder::getGroupRule, normalizeGroupRule(query.getGroupRule()));

        addItemFilters(lqw, query);
        if (StringUtils.isBlank(query.getSortBy()) || query.getIsAsc() == null) {
            lqw.orderByDesc(WaveOrder::getCreateTime);
        }

        IPage<WaveOrder> page = new Page<>(query.getPageNo(), query.getPageSize());
        OrderUtil.addOrder(page, query.getSortBy(), query.getIsAsc());

        IPage<WaveOrder> result = waveOrderMapper.selectPage(page, lqw);
        List<WaveVo> records = result.getRecords().stream()
                .map(this::toWaveVo)
                .filter(Objects::nonNull)
                .toList();
        fillWaveDisplay(records);
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    @Override
    public WaveDetailVo getDetailById(Long id) {
        WaveOrder wave = getById(id);
        if (wave == null) {
            return null;
        }

        WaveDetailVo detailVo = MapstructUtils.convert(wave, WaveDetailVo.class);
        if (detailVo == null) {
            return null;
        }
        fillWaveDisplay(List.of(detailVo));

        List<WaveOrderItem> items = waveOrderItemMapper.selectList(Wrappers.<WaveOrderItem>lambdaQuery()
                .eq(WaveOrderItem::getWaveId, id)
                .orderByAsc(WaveOrderItem::getLineNo, WaveOrderItem::getId));
        List<WaveOrderItemVo> itemVos = items.stream()
                .map(item -> MapstructUtils.convert(item, WaveOrderItemVo.class))
                .filter(Objects::nonNull)
                .toList();
        fillItemDisplay(itemVos);
        detailVo.setItems(itemVos);
        return detailVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createWave(WaveBo bo) {
        Long warehouseId = bo.getWarehouseId();
        validateWarehouse(warehouseId);

        String groupRule = normalizeGroupRule(bo.getGroupRule());
        List<OutboundOrder> orders = loadAndValidateOutboundOrders(warehouseId, bo.getOutboundOrderIds());
        ensureNoActiveWaveConflict(orders, null);
        List<WaveOrderItem> waveItems = buildWaveItems(orders, groupRule);

        WaveOrder wave = new WaveOrder();
        wave.setWaveNo(resolveWaveNo(bo.getWaveNo()));
        wave.setWarehouseId(warehouseId);
        wave.setStatus(WaveConstants.STATUS_DRAFT);
        wave.setGroupRule(groupRule);
        applySummary(wave, orders, waveItems);
        wave.setRemark(normalizeRemark(bo.getRemark()));
        if (!save(wave)) {
            throw new RuntimeException("波次创建失败");
        }

        saveWaveItems(wave.getId(), waveItems);
        return wave.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateWave(Long id, WaveBo bo) {
        WaveOrder wave = loadEditableWave(id);
        Long warehouseId = bo.getWarehouseId() == null ? wave.getWarehouseId() : bo.getWarehouseId();
        validateWarehouse(warehouseId);

        String groupRule = StringUtils.isBlank(bo.getGroupRule())
                ? wave.getGroupRule()
                : normalizeGroupRule(bo.getGroupRule());

        if (bo.getOutboundOrderIds() != null) {
            List<OutboundOrder> orders = loadAndValidateOutboundOrders(warehouseId, bo.getOutboundOrderIds());
            ensureNoActiveWaveConflict(orders, id);
            List<WaveOrderItem> waveItems = buildWaveItems(orders, groupRule);
            replaceWaveItems(id, waveItems);
            applySummary(wave, orders, waveItems);
        } else if (!Objects.equals(warehouseId, wave.getWarehouseId())) {
            List<Long> existingOrderIds = waveOrderItemMapper.selectList(Wrappers.<WaveOrderItem>lambdaQuery()
                            .select(WaveOrderItem::getOutboundOrderId)
                            .eq(WaveOrderItem::getWaveId, id))
                    .stream()
                    .map(WaveOrderItem::getOutboundOrderId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            loadAndValidateOutboundOrders(warehouseId, existingOrderIds);
        }

        wave.setWarehouseId(warehouseId);
        wave.setGroupRule(groupRule);
        if (bo.getRemark() != null) {
            wave.setRemark(normalizeRemark(bo.getRemark()));
        }
        if (!updateById(wave)) {
            throw new RuntimeException("波次更新失败");
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean applyAction(Long id, WaveActionBo bo) {
        WaveOrder wave = getById(id);
        if (wave == null) {
            throw new RuntimeException("波次不存在");
        }

        String action = StringUtils.trimToEmpty(bo.getAction()).toUpperCase(Locale.ROOT);
        if (WaveConstants.ACTION_RELEASE.equals(action)) {
            releaseWave(wave, bo);
            return true;
        }
        if (WaveConstants.ACTION_CANCEL.equals(action)) {
            cancelWave(wave, bo);
            return true;
        }
        if (WaveConstants.ACTION_GENERATE_PICKING.equals(action)) {
            pickingTaskService.createFromWave(id);
            return true;
        }
        throw new RuntimeException("不支持的动作: " + bo.getAction());
    }

    private void addItemFilters(LambdaQueryWrapper<WaveOrder> lqw, WaveQuery query) {
        if (query.getOutboundOrderId() != null) {
            lqw.inSql(WaveOrder::getId,
                    "select distinct wave_id from wave_order_item where outbound_order_id = "
                            + query.getOutboundOrderId());
        }
        if (query.getMaterialId() != null) {
            lqw.inSql(WaveOrder::getId,
                    "select distinct wave_id from wave_order_item where material_id = " + query.getMaterialId());
        }
        if (query.getAreaId() != null) {
            lqw.inSql(WaveOrder::getId,
                    "select distinct wave_id from wave_order_item where area_id = " + query.getAreaId());
        }
    }

    private WaveOrder loadEditableWave(Long id) {
        if (id == null) {
            throw new RuntimeException("波次ID不能为空");
        }
        WaveOrder wave = getById(id);
        if (wave == null) {
            throw new RuntimeException("波次不存在");
        }
        if (!WaveConstants.STATUS_DRAFT.equals(wave.getStatus())) {
            throw new RuntimeException("仅草稿波次允许编辑");
        }
        return wave;
    }

    private void releaseWave(WaveOrder wave, WaveActionBo bo) {
        if (!WaveConstants.STATUS_DRAFT.equals(wave.getStatus())) {
            if (WaveConstants.STATUS_RELEASED.equals(wave.getStatus())) {
                throw new RuntimeException("波次已发布");
            }
            throw new RuntimeException("当前状态不可发布");
        }
        long itemCount = waveOrderItemMapper.selectCount(Wrappers.<WaveOrderItem>lambdaQuery()
                .eq(WaveOrderItem::getWaveId, wave.getId()));
        if (itemCount <= 0) {
            throw new RuntimeException("波次明细不能为空");
        }

        wave.setStatus(WaveConstants.STATUS_RELEASED);
        wave.setReleaseTime(LocalDateTime.now());
        updateRemark(wave, bo);
        if (!updateById(wave)) {
            throw new RuntimeException("波次发布失败");
        }
        updateWaveItemStatus(wave.getId(), WaveConstants.STATUS_RELEASED);
    }

    private void cancelWave(WaveOrder wave, WaveActionBo bo) {
        if (WaveConstants.STATUS_CANCELLED.equals(wave.getStatus())) {
            throw new RuntimeException("波次已取消");
        }
        wave.setStatus(WaveConstants.STATUS_CANCELLED);
        wave.setCancelTime(LocalDateTime.now());
        updateRemark(wave, bo);
        if (!updateById(wave)) {
            throw new RuntimeException("波次取消失败");
        }
        updateWaveItemStatus(wave.getId(), WaveConstants.STATUS_CANCELLED);
    }

    private void validateWarehouse(Long warehouseId) {
        if (warehouseId == null) {
            throw new RuntimeException("仓库不能为空");
        }
        Warehouse warehouse = warehouseMapper.selectById(warehouseId);
        if (warehouse == null) {
            throw new RuntimeException("仓库不存在");
        }
    }

    private List<OutboundOrder> loadAndValidateOutboundOrders(Long warehouseId, List<Long> outboundOrderIds) {
        List<Long> normalizedIds = normalizeOutboundOrderIds(outboundOrderIds);
        List<OutboundOrder> orders = outboundOrderMapper.selectByIds(normalizedIds);
        Map<Long, OutboundOrder> orderMap = orders.stream()
                .collect(Collectors.toMap(OutboundOrder::getId, Function.identity(), (left, right) -> left));

        List<OutboundOrder> ordered = new ArrayList<>(normalizedIds.size());
        for (Long orderId : normalizedIds) {
            OutboundOrder order = orderMap.get(orderId);
            if (order == null) {
                throw new RuntimeException("出库单不存在: " + orderId);
            }
            if (!Objects.equals(order.getWarehouseId(), warehouseId)) {
                throw new RuntimeException("出库单不属于当前仓库: " + order.getOrderNo());
            }
            ordered.add(order);
        }
        return ordered;
    }

    private List<Long> normalizeOutboundOrderIds(List<Long> outboundOrderIds) {
        if (outboundOrderIds == null || outboundOrderIds.isEmpty()) {
            throw new RuntimeException("波次出库单不能为空");
        }
        List<Long> normalizedIds = outboundOrderIds.stream()
                .filter(Objects::nonNull)
                .toList();
        if (normalizedIds.size() != outboundOrderIds.size()) {
            throw new RuntimeException("出库单ID不能为空");
        }

        LinkedHashSet<Long> uniqueIds = new LinkedHashSet<>(normalizedIds);
        if (uniqueIds.size() != normalizedIds.size()) {
            throw new RuntimeException("出库单不能重复选择");
        }
        return new ArrayList<>(uniqueIds);
    }

    private void ensureNoActiveWaveConflict(List<OutboundOrder> orders, Long currentWaveId) {
        if (orders == null || orders.isEmpty()) {
            return;
        }

        Set<Long> orderIds = orders.stream()
                .map(OutboundOrder::getId)
                .collect(Collectors.toSet());
        List<WaveOrderItem> matchedItems = waveOrderItemMapper.selectList(Wrappers.<WaveOrderItem>lambdaQuery()
                .in(WaveOrderItem::getOutboundOrderId, orderIds)
                .ne(currentWaveId != null, WaveOrderItem::getWaveId, currentWaveId));
        if (matchedItems.isEmpty()) {
            return;
        }

        Set<Long> waveIds = matchedItems.stream()
                .map(WaveOrderItem::getWaveId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (waveIds.isEmpty()) {
            return;
        }

        Set<Long> activeWaveIds = waveOrderMapper.selectList(Wrappers.<WaveOrder>lambdaQuery()
                        .select(WaveOrder::getId)
                        .in(WaveOrder::getId, waveIds)
                        .ne(WaveOrder::getStatus, WaveConstants.STATUS_CANCELLED))
                .stream()
                .map(WaveOrder::getId)
                .collect(Collectors.toSet());
        if (activeWaveIds.isEmpty()) {
            return;
        }

        Map<Long, String> orderNoMap = orders.stream()
                .collect(Collectors.toMap(OutboundOrder::getId, OutboundOrder::getOrderNo, (left, right) -> left));
        WaveOrderItem conflictedItem = matchedItems.stream()
                .filter(item -> activeWaveIds.contains(item.getWaveId()))
                .findFirst()
                .orElse(null);
        if (conflictedItem != null) {
            String orderNo = orderNoMap.getOrDefault(conflictedItem.getOutboundOrderId(),
                    String.valueOf(conflictedItem.getOutboundOrderId()));
            throw new RuntimeException("出库单已加入未取消波次: " + orderNo);
        }
    }

    private List<WaveOrderItem> buildWaveItems(List<OutboundOrder> orders, String groupRule) {
        Map<Long, OutboundOrder> orderMap = orders.stream()
                .collect(Collectors.toMap(OutboundOrder::getId, Function.identity(),
                        (left, right) -> left, LinkedHashMap::new));
        Set<Long> orderIds = orderMap.keySet();
        if (orderIds.isEmpty()) {
            throw new RuntimeException("波次出库单不能为空");
        }

        List<OutboundOrderItem> outboundItems = outboundOrderItemMapper.selectList(Wrappers.<OutboundOrderItem>lambdaQuery()
                .in(OutboundOrderItem::getOrderId, orderIds)
                .orderByAsc(OutboundOrderItem::getOrderId, OutboundOrderItem::getLineNo, OutboundOrderItem::getId));
        if (outboundItems.isEmpty()) {
            throw new RuntimeException("波次出库单明细不能为空");
        }

        Set<Long> itemOrderIds = outboundItems.stream()
                .map(OutboundOrderItem::getOrderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        for (OutboundOrder order : orders) {
            if (!itemOrderIds.contains(order.getId())) {
                throw new RuntimeException("出库单明细不能为空: " + order.getOrderNo());
            }
        }

        Map<Long, Location> locationMap = loadLocationMap(outboundItems);
        List<WaveOrderItem> waveItems = new ArrayList<>(outboundItems.size());
        for (OutboundOrderItem outboundItem : outboundItems) {
            OutboundOrder order = orderMap.get(outboundItem.getOrderId());
            if (order == null) {
                continue;
            }
            Location location = locationMap.get(outboundItem.getLocationId());
            WaveOrderItem waveItem = new WaveOrderItem();
            waveItem.setOutboundOrderId(order.getId());
            waveItem.setOutboundOrderNo(order.getOrderNo());
            waveItem.setOutboundOrderItemId(outboundItem.getId());
            waveItem.setWarehouseId(order.getWarehouseId());
            waveItem.setAreaId(location == null ? null : location.getAreaId());
            waveItem.setLocationId(outboundItem.getLocationId());
            waveItem.setMaterialId(outboundItem.getMaterialId());
            waveItem.setBatchNo(normalizeBatchNo(outboundItem.getBatchNo()));
            waveItem.setProductionDate(outboundItem.getProductionDate());
            waveItem.setExpiryDate(outboundItem.getExpiryDate());
            waveItem.setPlannedQty(normalizeQuantity(outboundItem.getPlannedQty()));
            waveItem.setStatus(WaveConstants.STATUS_DRAFT);
            waveItem.setRemark(StringUtils.defaultString(StringUtils.trimToNull(outboundItem.getRemark())));
            waveItems.add(waveItem);
        }

        sortWaveItems(waveItems, orderMap, groupRule);
        for (int i = 0; i < waveItems.size(); i++) {
            waveItems.get(i).setLineNo(i + 1);
        }
        return waveItems;
    }

    private Map<Long, Location> loadLocationMap(List<OutboundOrderItem> outboundItems) {
        Set<Long> locationIds = outboundItems.stream()
                .map(OutboundOrderItem::getLocationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (locationIds.isEmpty()) {
            return Map.of();
        }
        return locationMapper.selectByIds(locationIds).stream()
                .collect(Collectors.toMap(Location::getId, Function.identity(), (left, right) -> left));
    }

    private void sortWaveItems(List<WaveOrderItem> waveItems, Map<Long, OutboundOrder> orderMap, String groupRule) {
        if (WaveConstants.GROUP_RULE_MANUAL.equals(groupRule)) {
            Map<Long, Integer> orderIndexMap = new LinkedHashMap<>();
            int index = 0;
            for (Long orderId : orderMap.keySet()) {
                orderIndexMap.put(orderId, index++);
            }
            waveItems.sort(Comparator
                    .comparing((WaveOrderItem item) -> orderIndexMap.get(item.getOutboundOrderId()))
                    .thenComparing(WaveOrderItem::getOutboundOrderItemId));
            return;
        }
        if (WaveConstants.GROUP_RULE_BY_ORDER.equals(groupRule)) {
            waveItems.sort(Comparator
                    .comparing(WaveOrderItem::getOutboundOrderNo, Comparator.nullsLast(String::compareTo))
                    .thenComparing(WaveOrderItem::getOutboundOrderItemId));
            return;
        }
        if (WaveConstants.GROUP_RULE_BY_SKU.equals(groupRule)) {
            waveItems.sort(Comparator
                    .comparing(WaveOrderItem::getMaterialId, Comparator.nullsLast(Long::compareTo))
                    .thenComparing(WaveOrderItem::getOutboundOrderNo, Comparator.nullsLast(String::compareTo))
                    .thenComparing(WaveOrderItem::getOutboundOrderItemId));
            return;
        }
        waveItems.sort(Comparator
                .comparing(WaveOrderItem::getAreaId, Comparator.nullsLast(Long::compareTo))
                .thenComparing(WaveOrderItem::getLocationId, Comparator.nullsLast(Long::compareTo))
                .thenComparing(WaveOrderItem::getMaterialId, Comparator.nullsLast(Long::compareTo))
                .thenComparing(WaveOrderItem::getOutboundOrderNo, Comparator.nullsLast(String::compareTo))
                .thenComparing(WaveOrderItem::getOutboundOrderItemId));
    }

    private void saveWaveItems(Long waveId, List<WaveOrderItem> items) {
        for (WaveOrderItem item : items) {
            item.setWaveId(waveId);
            waveOrderItemMapper.insert(item);
        }
    }

    private void replaceWaveItems(Long waveId, List<WaveOrderItem> items) {
        waveOrderItemMapper.delete(Wrappers.<WaveOrderItem>lambdaQuery().eq(WaveOrderItem::getWaveId, waveId));
        saveWaveItems(waveId, items);
    }

    private void applySummary(WaveOrder wave, List<OutboundOrder> orders, List<WaveOrderItem> items) {
        wave.setTotalOrders(orders == null ? 0 : orders.size());
        wave.setTotalItems(items == null ? 0 : items.size());
        BigDecimal totalQty = items == null
                ? BigDecimal.ZERO
                : items.stream()
                .map(WaveOrderItem::getPlannedQty)
                .map(this::normalizeQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        wave.setTotalQty(totalQty);
    }

    private void updateWaveItemStatus(Long waveId, String status) {
        List<WaveOrderItem> items = waveOrderItemMapper.selectList(Wrappers.<WaveOrderItem>lambdaQuery()
                .eq(WaveOrderItem::getWaveId, waveId));
        for (WaveOrderItem item : items) {
            item.setStatus(status);
            if (waveOrderItemMapper.updateById(item) != 1) {
                throw new RuntimeException("波次明细状态更新失败");
            }
        }
    }

    private void fillWaveDisplay(List<? extends WaveVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        fillWarehouseDisplay(records);
        fillPickingTaskDisplay(records);
        for (WaveVo record : records) {
            record.setTotalOrders(record.getTotalOrders() == null ? 0 : record.getTotalOrders());
            record.setTotalItems(record.getTotalItems() == null ? 0 : record.getTotalItems());
            record.setTotalQty(normalizeQuantity(record.getTotalQty()));
            record.setOrderCount(record.getTotalOrders());
            record.setItemCount(record.getTotalItems());
            record.setPickingGenerated(Boolean.TRUE.equals(record.getPickingGenerated()));
        }
    }

    private void fillPickingTaskDisplay(List<? extends WaveVo> records) {
        Set<Long> waveIds = records.stream()
                .map(WaveVo::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (waveIds.isEmpty()) {
            return;
        }

        Map<Long, PickingTask> taskMap = pickingTaskService.list(Wrappers.<PickingTask>lambdaQuery()
                        .in(PickingTask::getWaveId, waveIds)
                        .ne(PickingTask::getStatus, PickingTaskStatusConst.CANCELLED)
                        .orderByDesc(PickingTask::getCreateTime))
                .stream()
                .collect(Collectors.toMap(PickingTask::getWaveId, Function.identity(), (left, right) -> left));
        for (WaveVo record : records) {
            PickingTask task = taskMap.get(record.getId());
            if (task == null) {
                record.setPickingGenerated(false);
                continue;
            }
            record.setPickingTaskId(task.getId());
            record.setPickingTaskNo(task.getTaskNo());
            record.setPickingGenerated(true);
        }
    }

    private void fillWarehouseDisplay(List<? extends WaveVo> records) {
        Set<Long> warehouseIds = records.stream()
                .map(WaveVo::getWarehouseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (warehouseIds.isEmpty()) {
            return;
        }
        Map<Long, Warehouse> warehouseMap = warehouseMapper.selectByIds(warehouseIds).stream()
                .collect(Collectors.toMap(Warehouse::getId, Function.identity(), (left, right) -> left));
        for (WaveVo record : records) {
            Warehouse warehouse = warehouseMap.get(record.getWarehouseId());
            if (warehouse != null) {
                record.setWarehouseCode(warehouse.getWarehouseCode());
                record.setWarehouseName(warehouse.getWarehouseName());
            }
        }
    }

    private void fillItemDisplay(List<WaveOrderItemVo> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        fillOutboundOrderDisplay(items);
        fillLineDisplay(items);
        for (WaveOrderItemVo item : items) {
            item.setPlannedQty(normalizeQuantity(item.getPlannedQty()));
        }
    }

    private void fillOutboundOrderDisplay(List<WaveOrderItemVo> items) {
        Set<Long> orderIds = items.stream()
                .map(WaveOrderItemVo::getOutboundOrderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (orderIds.isEmpty()) {
            return;
        }
        Map<Long, OutboundOrder> orderMap = outboundOrderMapper.selectByIds(orderIds).stream()
                .collect(Collectors.toMap(OutboundOrder::getId, Function.identity(), (left, right) -> left));
        for (WaveOrderItemVo item : items) {
            OutboundOrder order = orderMap.get(item.getOutboundOrderId());
            if (order != null) {
                item.setOutboundOrderNo(order.getOrderNo());
                item.setOutboundOrderStatus(order.getStatus());
            }
        }
    }

    private void fillLineDisplay(List<WaveOrderItemVo> items) {
        Set<Long> areaIds = items.stream()
                .map(WaveOrderItemVo::getAreaId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> locationIds = items.stream()
                .map(WaveOrderItemVo::getLocationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> materialIds = items.stream()
                .map(WaveOrderItemVo::getMaterialId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Area> areaMap = areaIds.isEmpty()
                ? Map.of()
                : areaMapper.selectByIds(areaIds).stream()
                .collect(Collectors.toMap(Area::getId, Function.identity(), (left, right) -> left));
        Map<Long, Location> locationMap = locationIds.isEmpty()
                ? Map.of()
                : locationMapper.selectByIds(locationIds).stream()
                .collect(Collectors.toMap(Location::getId, Function.identity(), (left, right) -> left));
        Map<Long, Material> materialMap = materialIds.isEmpty()
                ? Map.of()
                : materialMapper.selectByIds(materialIds).stream()
                .collect(Collectors.toMap(Material::getId, Function.identity(), (left, right) -> left));

        for (WaveOrderItemVo item : items) {
            Area area = areaMap.get(item.getAreaId());
            if (area != null) {
                item.setAreaCode(area.getAreaCode());
                item.setAreaName(area.getAreaName());
            }
            Location location = locationMap.get(item.getLocationId());
            if (location != null) {
                item.setLocationCode(location.getLocationCode());
                item.setLocationName(location.getLocationName());
            }
            Material material = materialMap.get(item.getMaterialId());
            if (material != null) {
                item.setMaterialCode(material.getMaterialCode());
                item.setMaterialName(material.getMaterialName());
            }
        }
    }

    private WaveVo toWaveVo(WaveOrder wave) {
        WaveVo vo = MapstructUtils.convert(wave, WaveVo.class);
        if (vo != null) {
            vo.setTotalOrders(vo.getTotalOrders() == null ? 0 : vo.getTotalOrders());
            vo.setTotalItems(vo.getTotalItems() == null ? 0 : vo.getTotalItems());
            vo.setTotalQty(normalizeQuantity(vo.getTotalQty()));
            vo.setOrderCount(vo.getTotalOrders());
            vo.setItemCount(vo.getTotalItems());
        }
        return vo;
    }

    private void updateRemark(WaveOrder wave, WaveActionBo bo) {
        String remark = StringUtils.trimToNull(bo.getRemark());
        if (remark != null) {
            wave.setRemark(remark);
        }
    }

    private String resolveWaveNo(String waveNo) {
        String normalized = StringUtils.trimToNull(waveNo);
        if (normalized != null) {
            return normalized;
        }
        return CodeGenerate.generateSimple(WaveConstants.CODE_PREFIX);
    }

    private String normalizeStatus(String status) {
        String normalized = WaveConstants.normalizeStatus(status);
        if (!WaveConstants.isValidStatus(normalized)) {
            throw new RuntimeException("不支持的波次状态: " + status);
        }
        return normalized;
    }

    private String normalizeGroupRule(String groupRule) {
        String normalized = WaveConstants.normalizeGroupRule(groupRule);
        if (!WaveConstants.isValidGroupRule(normalized)) {
            throw new RuntimeException("不支持的分组规则: " + groupRule);
        }
        return normalized;
    }

    private BigDecimal normalizeQuantity(BigDecimal quantity) {
        return quantity == null ? BigDecimal.ZERO : quantity;
    }

    private String normalizeRemark(String remark) {
        return StringUtils.defaultString(StringUtils.trimToNull(remark));
    }

    private String normalizeBatchNo(String batchNo) {
        return StringUtils.defaultString(StringUtils.trimToNull(batchNo));
    }
}
