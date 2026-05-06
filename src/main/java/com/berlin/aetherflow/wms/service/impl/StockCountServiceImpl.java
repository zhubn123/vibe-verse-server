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
import com.berlin.aetherflow.wms.constant.StockBizTypeConst;
import com.berlin.aetherflow.wms.constant.StockCountStatusConst;
import com.berlin.aetherflow.wms.domain.bo.StockChangeBo;
import com.berlin.aetherflow.wms.domain.bo.StockCountActionBo;
import com.berlin.aetherflow.wms.domain.bo.StockCountCreateBo;
import com.berlin.aetherflow.wms.domain.bo.StockCountItemBo;
import com.berlin.aetherflow.wms.domain.bo.StockCountItemsBo;
import com.berlin.aetherflow.wms.domain.bo.StockCountReviewItemBo;
import com.berlin.aetherflow.wms.domain.bo.StockCountReviewItemsBo;
import com.berlin.aetherflow.wms.domain.entity.Area;
import com.berlin.aetherflow.wms.domain.entity.Inventory;
import com.berlin.aetherflow.wms.domain.entity.Location;
import com.berlin.aetherflow.wms.domain.entity.Material;
import com.berlin.aetherflow.wms.domain.entity.StockCountItem;
import com.berlin.aetherflow.wms.domain.entity.StockCountOrder;
import com.berlin.aetherflow.wms.domain.entity.Warehouse;
import com.berlin.aetherflow.wms.domain.query.StockCountQuery;
import com.berlin.aetherflow.wms.domain.vo.StockCountDetailVo;
import com.berlin.aetherflow.wms.domain.vo.StockCountItemVo;
import com.berlin.aetherflow.wms.domain.vo.StockCountVo;
import com.berlin.aetherflow.wms.mapper.AreaMapper;
import com.berlin.aetherflow.wms.mapper.InventoryMapper;
import com.berlin.aetherflow.wms.mapper.LocationMapper;
import com.berlin.aetherflow.wms.mapper.MaterialMapper;
import com.berlin.aetherflow.wms.mapper.StockCountOrderMapper;
import com.berlin.aetherflow.wms.mapper.WarehouseMapper;
import com.berlin.aetherflow.wms.service.InventoryService;
import com.berlin.aetherflow.wms.service.StockCountItemService;
import com.berlin.aetherflow.wms.service.StockCountService;
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
 * 盘点单 Service 实现。
 */
@Service
@AllArgsConstructor
public class StockCountServiceImpl extends ServiceImpl<StockCountOrderMapper, StockCountOrder>
        implements StockCountService {

    private static final String ACTION_ADJUST = "ADJUST";
    private static final String ACTION_CONFIRM = "CONFIRM";
    private static final String ACTION_CANCEL = "CANCEL";
    private static final String ACTION_SUBMIT_REVIEW = "SUBMIT_REVIEW";
    private static final String ACTION_APPROVE = "APPROVE";
    private static final String ACTION_REJECT = "REJECT";

    private final StockCountOrderMapper stockCountOrderMapper;
    private final StockCountItemService stockCountItemService;
    private final InventoryMapper inventoryMapper;
    private final InventoryService inventoryService;
    private final WarehouseMapper warehouseMapper;
    private final AreaMapper areaMapper;
    private final LocationMapper locationMapper;
    private final MaterialMapper materialMapper;

    @Override
    public PageResult<StockCountVo> queryList(StockCountQuery query) {
        LambdaQueryWrapper<StockCountOrder> lqw = Wrappers.<StockCountOrder>lambdaQuery()
                .like(StringUtils.isNotBlank(query.getCountNo()), StockCountOrder::getCountNo, query.getCountNo())
                .eq(query.getWarehouseId() != null, StockCountOrder::getWarehouseId, query.getWarehouseId())
                .eq(StringUtils.isNotBlank(query.getStatus()), StockCountOrder::getStatus, normalizeStatus(query.getStatus()));

        Set<Long> matchedCountIds = findMatchedCountIds(query);
        if (matchedCountIds != null) {
            if (matchedCountIds.isEmpty()) {
                return PageResult.of(Long.valueOf(query.getPageNo()), Long.valueOf(query.getPageSize()), 0L, 0L, List.of());
            }
            lqw.in(StockCountOrder::getId, matchedCountIds);
        }

        if (StringUtils.isBlank(query.getSortBy()) || query.getIsAsc() == null) {
            lqw.orderByDesc(StockCountOrder::getCreateTime);
        }
        IPage<StockCountOrder> page = new Page<>(query.getPageNo(), query.getPageSize());
        OrderUtil.addOrder(page, query.getSortBy(), query.getIsAsc());

        IPage<StockCountOrder> result = stockCountOrderMapper.selectPage(page, lqw);
        List<StockCountVo> records = result.getRecords().stream()
                .map(this::toStockCountVo)
                .filter(Objects::nonNull)
                .toList();
        fillHeaderDisplay(records);
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    @Override
    public StockCountDetailVo getDetailById(Long id) {
        StockCountOrder order = getById(id);
        if (order == null) {
            return null;
        }
        StockCountDetailVo detailVo = MapstructUtils.convert(order, StockCountDetailVo.class);
        if (detailVo == null) {
            return null;
        }
        normalizeHeader(detailVo);
        fillHeaderDisplay(List.of(detailVo));

        List<StockCountItem> items = stockCountItemService.lambdaQuery()
                .eq(StockCountItem::getCountId, id)
                .orderByAsc(StockCountItem::getLineNo)
                .list();
        List<StockCountItemVo> itemVos = items.stream()
                .map(item -> MapstructUtils.convert(item, StockCountItemVo.class))
                .filter(Objects::nonNull)
                .toList();
        fillItemDisplay(itemVos);
        detailVo.setItems(itemVos);
        return detailVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createStockCount(StockCountCreateBo bo) {
        validateCreateBo(bo);
        List<Inventory> inventories = loadInventorySnapshot(bo);
        if (inventories.isEmpty()) {
            throw new RuntimeException("没有匹配的可盘点库存");
        }

        Map<Long, Location> locationMap = loadLocationMap(inventories.stream()
                .map(Inventory::getLocationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet()));
        BigDecimal expectedQty = inventories.stream()
                .map(Inventory::getQuantity)
                .map(this::normalizeQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StockCountOrder order = new StockCountOrder();
        order.setCountNo(CodeGenerate.generateSimple(BizCodeTypeConst.INVENTORY_COUNT));
        order.setWarehouseId(bo.getWarehouseId());
        order.setStatus(StockCountStatusConst.PENDING);
        order.setTotalItems(inventories.size());
        order.setDifferenceItems(0);
        order.setExpectedQty(expectedQty);
        order.setCountedQty(BigDecimal.ZERO);
        order.setDifferenceQty(BigDecimal.ZERO);
        order.setCountTime(LocalDateTime.now());
        order.setRemark(StringUtils.defaultString(StringUtils.trimToNull(bo.getRemark())));
        boolean saved = save(order);
        if (!saved) {
            throw new RuntimeException("盘点单创建失败");
        }

        List<StockCountItem> items = new ArrayList<>(inventories.size());
        for (int i = 0; i < inventories.size(); i++) {
            Inventory inventory = inventories.get(i);
            Location location = locationMap.get(inventory.getLocationId());
            StockCountItem item = new StockCountItem();
            item.setCountId(order.getId());
            item.setInventoryId(inventory.getId());
            item.setLineNo(i + 1);
            item.setWarehouseId(inventory.getWarehouseId());
            item.setAreaId(location == null ? null : location.getAreaId());
            item.setLocationId(inventory.getLocationId());
            item.setMaterialId(inventory.getMaterialId());
            item.setBatchNo(normalizeBatchNo(inventory.getBatchNo()));
            item.setProductionDate(inventory.getProductionDate());
            item.setExpiryDate(inventory.getExpiryDate());
            item.setExpectedQty(normalizeQuantity(inventory.getQuantity()));
            item.setDifferenceQty(BigDecimal.ZERO);
            item.setDifferenceReason("");
            item.setReviewRemark("");
            item.setRemark("");
            items.add(item);
        }
        boolean itemsSaved = stockCountItemService.saveBatch(items);
        if (!itemsSaved) {
            throw new RuntimeException("盘点单明细创建失败");
        }
        return order.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveCountItems(Long id, StockCountItemsBo bo) {
        StockCountOrder order = loadPendingOrder(id);
        Map<Long, StockCountItem> itemMap = stockCountItemService.lambdaQuery()
                .eq(StockCountItem::getCountId, id)
                .list()
                .stream()
                .collect(Collectors.toMap(StockCountItem::getId, item -> item, (left, right) -> left));
        if (itemMap.isEmpty()) {
            throw new RuntimeException("盘点单明细不能为空");
        }

        List<StockCountItem> updatedItems = new ArrayList<>(bo.getItems().size());
        for (StockCountItemBo itemBo : bo.getItems()) {
            StockCountItem item = itemMap.get(itemBo.getId());
            if (item == null) {
                throw new RuntimeException("盘点明细不存在或不属于当前盘点单: " + itemBo.getId());
            }
            BigDecimal countedQty = normalizeQuantity(itemBo.getCountedQty());
            item.setCountedQty(countedQty);
            item.setReviewCountedQty(null);
            item.setDifferenceQty(countedQty.subtract(normalizeQuantity(item.getExpectedQty())));
            item.setDifferenceReason("");
            item.setReviewRemark("");
            item.setRemark(StringUtils.defaultString(StringUtils.trimToNull(itemBo.getRemark())));
            updatedItems.add(item);
        }

        boolean updated = stockCountItemService.updateBatchById(updatedItems);
        if (!updated) {
            throw new RuntimeException("盘点明细保存失败");
        }
        refreshOrderSummary(order);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveReviewItems(Long id, StockCountReviewItemsBo bo) {
        StockCountOrder order = loadReviewingOrder(id);
        Map<Long, StockCountItem> itemMap = stockCountItemService.lambdaQuery()
                .eq(StockCountItem::getCountId, id)
                .list()
                .stream()
                .collect(Collectors.toMap(StockCountItem::getId, item -> item, (left, right) -> left));
        if (itemMap.isEmpty()) {
            throw new RuntimeException("盘点单明细不能为空");
        }

        List<StockCountItem> updatedItems = new ArrayList<>(bo.getItems().size());
        for (StockCountReviewItemBo itemBo : bo.getItems()) {
            StockCountItem item = itemMap.get(itemBo.getId());
            if (item == null) {
                throw new RuntimeException("盘点明细不存在或不属于当前盘点单: " + itemBo.getId());
            }
            if (item.getCountedQty() == null) {
                throw new RuntimeException("存在未录入初盘数量的明细，行号: " + item.getLineNo());
            }
            BigDecimal reviewCountedQty = normalizeQuantity(itemBo.getReviewCountedQty());
            item.setReviewCountedQty(reviewCountedQty);
            item.setDifferenceQty(reviewCountedQty.subtract(normalizeQuantity(item.getExpectedQty())));
            item.setDifferenceReason(StringUtils.defaultString(StringUtils.trimToNull(itemBo.getDifferenceReason())));
            item.setReviewRemark(StringUtils.defaultString(StringUtils.trimToNull(itemBo.getReviewRemark())));
            updatedItems.add(item);
        }

        boolean updated = stockCountItemService.updateBatchById(updatedItems);
        if (!updated) {
            throw new RuntimeException("盘点复盘明细保存失败");
        }
        refreshOrderSummary(order);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean applyAction(Long id, StockCountActionBo bo) {
        String action = StringUtils.trimToEmpty(bo.getAction()).toUpperCase(Locale.ROOT);
        if (ACTION_ADJUST.equals(action) || ACTION_CONFIRM.equals(action)) {
            return adjustStockCount(id, bo);
        }
        if (ACTION_CANCEL.equals(action)) {
            return cancelStockCount(id, bo);
        }
        if (ACTION_SUBMIT_REVIEW.equals(action)) {
            return submitReview(id, bo);
        }
        if (ACTION_APPROVE.equals(action)) {
            return approveStockCount(id, bo);
        }
        if (ACTION_REJECT.equals(action)) {
            return rejectStockCount(id, bo);
        }
        throw new RuntimeException("不支持的动作: " + bo.getAction());
    }

    private Boolean submitReview(Long id, StockCountActionBo bo) {
        StockCountOrder order = loadPendingOrder(id);
        List<StockCountItem> items = loadStockCountItems(id);
        ensureAllCounted(items);
        recalculateItemDifferences(items);
        refreshOrderSummary(order);

        order.setStatus(StockCountStatusConst.REVIEWING);
        order.setReviewSubmitTime(LocalDateTime.now());
        order.setReviewTime(null);
        order.setReviewBy("");
        order.setReviewRemark("");
        updateRemark(order, bo);
        if (!updateById(order)) {
            throw new RuntimeException("盘点单提交复盘失败");
        }
        return true;
    }

    private Boolean approveStockCount(Long id, StockCountActionBo bo) {
        StockCountOrder order = loadReviewingOrder(id);
        List<StockCountItem> items = loadStockCountItems(id);
        ensureAllCounted(items);
        recalculateItemDifferences(items);
        refreshOrderSummary(order);

        order.setStatus(StockCountStatusConst.APPROVED);
        updateReviewAuditInfo(order, bo, LocalDateTime.now());
        if (!updateById(order)) {
            throw new RuntimeException("盘点单审批失败");
        }
        return true;
    }

    private Boolean rejectStockCount(Long id, StockCountActionBo bo) {
        StockCountOrder order = loadReviewingOrder(id);
        List<StockCountItem> items = loadStockCountItems(id);
        resetReviewItems(items);
        refreshOrderSummary(order);

        order.setStatus(StockCountStatusConst.PENDING);
        updateReviewAuditInfo(order, bo, LocalDateTime.now());
        if (!updateById(order)) {
            throw new RuntimeException("盘点单驳回失败");
        }
        return true;
    }

    private Boolean adjustStockCount(Long id, StockCountActionBo bo) {
        StockCountOrder order = loadAdjustableOrder(id);
        List<StockCountItem> items = loadStockCountItems(id);
        ensureAllCounted(items);
        recalculateItemDifferences(items);

        LocalDateTime operateTime = LocalDateTime.now();
        List<StockChangeBo> changes = items.stream()
                .filter(item -> normalizeQuantity(item.getDifferenceQty()).compareTo(BigDecimal.ZERO) != 0)
                .map(item -> toStockChange(order, item, operateTime))
                .toList();
        inventoryService.applyStockChanges(changes);

        refreshOrderSummary(order);
        order.setStatus(StockCountStatusConst.ADJUSTED);
        order.setAdjustTime(operateTime);
        updateRemark(order, bo);
        if (!updateById(order)) {
            throw new RuntimeException("盘点单状态更新失败");
        }
        return true;
    }

    private Boolean cancelStockCount(Long id, StockCountActionBo bo) {
        StockCountOrder order = loadCancellableOrder(id);
        order.setStatus(StockCountStatusConst.CANCELLED);
        updateRemark(order, bo);
        if (!updateById(order)) {
            throw new RuntimeException("盘点单取消失败");
        }
        return true;
    }

    private StockCountOrder loadPendingOrder(Long id) {
        StockCountOrder order = loadStockCountOrder(id);
        if (!StockCountStatusConst.PENDING.equals(order.getStatus())) {
            throw new RuntimeException("当前状态不可操作");
        }
        return order;
    }

    private StockCountOrder loadReviewingOrder(Long id) {
        StockCountOrder order = loadStockCountOrder(id);
        if (!StockCountStatusConst.REVIEWING.equals(order.getStatus())) {
            throw new RuntimeException("当前状态不可复盘审批");
        }
        return order;
    }

    private StockCountOrder loadAdjustableOrder(Long id) {
        StockCountOrder order = loadStockCountOrder(id);
        if (StockCountStatusConst.ADJUSTED.equals(order.getStatus())) {
            throw new RuntimeException("盘点单已调账，请勿重复提交");
        }
        if (StockCountStatusConst.CANCELLED.equals(order.getStatus())) {
            throw new RuntimeException("盘点单已取消，不能调账");
        }
        if (StockCountStatusConst.REVIEWING.equals(order.getStatus())) {
            throw new RuntimeException("盘点单复盘中，审批通过后才能调账");
        }
        if (!StockCountStatusConst.PENDING.equals(order.getStatus())
                && !StockCountStatusConst.APPROVED.equals(order.getStatus())) {
            throw new RuntimeException("当前状态不可调账");
        }
        return order;
    }

    private StockCountOrder loadCancellableOrder(Long id) {
        StockCountOrder order = loadStockCountOrder(id);
        if (StockCountStatusConst.ADJUSTED.equals(order.getStatus())) {
            throw new RuntimeException("盘点单已调账，不能取消");
        }
        if (StockCountStatusConst.CANCELLED.equals(order.getStatus())) {
            throw new RuntimeException("盘点单已取消，请勿重复提交");
        }
        if (!StockCountStatusConst.PENDING.equals(order.getStatus())
                && !StockCountStatusConst.REVIEWING.equals(order.getStatus())
                && !StockCountStatusConst.APPROVED.equals(order.getStatus())) {
            throw new RuntimeException("当前状态不可取消");
        }
        return order;
    }

    private StockCountOrder loadStockCountOrder(Long id) {
        if (id == null) {
            throw new RuntimeException("盘点单ID不能为空");
        }
        StockCountOrder order = getById(id);
        if (order == null) {
            throw new RuntimeException("盘点单不存在");
        }
        return order;
    }

    private List<StockCountItem> loadStockCountItems(Long id) {
        List<StockCountItem> items = stockCountItemService.lambdaQuery()
                .eq(StockCountItem::getCountId, id)
                .orderByAsc(StockCountItem::getLineNo)
                .list();
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("盘点单明细不能为空");
        }
        return items;
    }

    private List<Inventory> loadInventorySnapshot(StockCountCreateBo bo) {
        LambdaQueryWrapper<Inventory> lqw = Wrappers.<Inventory>lambdaQuery()
                .eq(Inventory::getWarehouseId, bo.getWarehouseId())
                .eq(bo.getLocationId() != null, Inventory::getLocationId, bo.getLocationId())
                .eq(bo.getMaterialId() != null, Inventory::getMaterialId, bo.getMaterialId())
                .like(StringUtils.isNotBlank(bo.getBatchNo()), Inventory::getBatchNo, bo.getBatchNo())
                .gt(Inventory::getQuantity, BigDecimal.ZERO)
                .orderByAsc(Inventory::getLocationId, Inventory::getMaterialId, Inventory::getBatchNo, Inventory::getId);

        if (bo.getAreaId() != null) {
            List<Long> locationIds = locationMapper.selectList(Wrappers.<Location>lambdaQuery()
                            .select(Location::getId)
                            .eq(Location::getWarehouseId, bo.getWarehouseId())
                            .eq(Location::getAreaId, bo.getAreaId()))
                    .stream()
                    .map(Location::getId)
                    .toList();
            if (locationIds.isEmpty()) {
                return List.of();
            }
            lqw.in(Inventory::getLocationId, locationIds);
        }
        return inventoryMapper.selectList(lqw);
    }

    private void validateCreateBo(StockCountCreateBo bo) {
        Warehouse warehouse = warehouseMapper.selectById(bo.getWarehouseId());
        if (warehouse == null) {
            throw new RuntimeException("仓库不存在");
        }
        if (bo.getAreaId() != null) {
            Area area = areaMapper.selectById(bo.getAreaId());
            if (area == null) {
                throw new RuntimeException("区域不存在");
            }
            if (!Objects.equals(area.getWarehouseId(), bo.getWarehouseId())) {
                throw new RuntimeException("区域不属于当前仓库");
            }
        }
        if (bo.getLocationId() != null) {
            Location location = locationMapper.selectById(bo.getLocationId());
            if (location == null) {
                throw new RuntimeException("库位不存在");
            }
            if (!Objects.equals(location.getWarehouseId(), bo.getWarehouseId())) {
                throw new RuntimeException("库位不属于当前仓库");
            }
            if (bo.getAreaId() != null && !Objects.equals(location.getAreaId(), bo.getAreaId())) {
                throw new RuntimeException("库位不属于当前区域");
            }
        }
        if (bo.getMaterialId() != null && materialMapper.selectById(bo.getMaterialId()) == null) {
            throw new RuntimeException("物料不存在");
        }
    }

    private Set<Long> findMatchedCountIds(StockCountQuery query) {
        boolean hasItemFilter = query.getAreaId() != null
                || query.getLocationId() != null
                || query.getMaterialId() != null
                || StringUtils.isNotBlank(query.getBatchNo());
        if (!hasItemFilter) {
            return null;
        }
        return stockCountItemService.list(Wrappers.<StockCountItem>lambdaQuery()
                        .select(StockCountItem::getCountId)
                        .eq(query.getAreaId() != null, StockCountItem::getAreaId, query.getAreaId())
                        .eq(query.getLocationId() != null, StockCountItem::getLocationId, query.getLocationId())
                        .eq(query.getMaterialId() != null, StockCountItem::getMaterialId, query.getMaterialId())
                        .like(StringUtils.isNotBlank(query.getBatchNo()), StockCountItem::getBatchNo, query.getBatchNo()))
                .stream()
                .map(StockCountItem::getCountId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private void refreshOrderSummary(StockCountOrder order) {
        List<StockCountItem> items = stockCountItemService.lambdaQuery()
                .eq(StockCountItem::getCountId, order.getId())
                .list();
        BigDecimal expectedQty = BigDecimal.ZERO;
        BigDecimal countedQty = BigDecimal.ZERO;
        BigDecimal differenceQty = BigDecimal.ZERO;
        int differenceItems = 0;
        for (StockCountItem item : items) {
            expectedQty = expectedQty.add(normalizeQuantity(item.getExpectedQty()));
            BigDecimal effectiveCountedQty = resolveEffectiveCountedQty(item);
            if (effectiveCountedQty != null) {
                countedQty = countedQty.add(effectiveCountedQty);
            }
            BigDecimal itemDiff = item.getDifferenceQty() == null ? BigDecimal.ZERO : item.getDifferenceQty();
            differenceQty = differenceQty.add(itemDiff);
            if (itemDiff.compareTo(BigDecimal.ZERO) != 0) {
                differenceItems++;
            }
        }
        order.setTotalItems(items.size());
        order.setDifferenceItems(differenceItems);
        order.setExpectedQty(expectedQty);
        order.setCountedQty(countedQty);
        order.setDifferenceQty(differenceQty);
        if (!updateById(order)) {
            throw new RuntimeException("盘点单汇总更新失败");
        }
    }

    private void ensureAllCounted(List<StockCountItem> items) {
        StockCountItem uncounted = items.stream()
                .filter(item -> item.getCountedQty() == null)
                .findFirst()
                .orElse(null);
        if (uncounted != null) {
            throw new RuntimeException("存在未录入实盘数量的明细，行号: " + uncounted.getLineNo());
        }
    }

    private void recalculateItemDifferences(List<StockCountItem> items) {
        boolean changed = false;
        for (StockCountItem item : items) {
            BigDecimal effectiveCountedQty = resolveEffectiveCountedQty(item);
            if (effectiveCountedQty == null) {
                continue;
            }
            BigDecimal differenceQty = effectiveCountedQty.subtract(normalizeQuantity(item.getExpectedQty()));
            if (!quantityEquals(item.getDifferenceQty(), differenceQty)) {
                item.setDifferenceQty(differenceQty);
                changed = true;
            }
        }
        if (changed && !stockCountItemService.updateBatchById(items)) {
            throw new RuntimeException("盘点明细差异数量更新失败");
        }
    }

    private void resetReviewItems(List<StockCountItem> items) {
        for (StockCountItem item : items) {
            BigDecimal countedQty = item.getCountedQty();
            item.setReviewCountedQty(null);
            item.setDifferenceQty(countedQty == null
                    ? BigDecimal.ZERO
                    : normalizeQuantity(countedQty).subtract(normalizeQuantity(item.getExpectedQty())));
            item.setDifferenceReason("");
            item.setReviewRemark("");
        }
        if (!stockCountItemService.updateBatchById(items)) {
            throw new RuntimeException("盘点复盘明细重置失败");
        }
    }

    private BigDecimal resolveEffectiveCountedQty(StockCountItem item) {
        return item.getReviewCountedQty() != null ? item.getReviewCountedQty() : item.getCountedQty();
    }

    private boolean quantityEquals(BigDecimal left, BigDecimal right) {
        return normalizeQuantity(left).compareTo(normalizeQuantity(right)) == 0;
    }

    private StockChangeBo toStockChange(StockCountOrder order, StockCountItem item, LocalDateTime operateTime) {
        StockChangeBo change = new StockChangeBo();
        change.setBizType(StockBizTypeConst.STOCK_COUNT);
        change.setBizId(order.getId());
        change.setWarehouseId(item.getWarehouseId());
        change.setLocationId(item.getLocationId());
        change.setMaterialId(item.getMaterialId());
        change.setBatchNo(normalizeBatchNo(item.getBatchNo()));
        change.setProductionDate(item.getProductionDate());
        change.setExpiryDate(item.getExpiryDate());
        change.setLineNo(item.getLineNo());
        change.setChangeQty(normalizeQuantity(item.getDifferenceQty()));
        change.setOperateTime(operateTime);
        change.setRemark(buildTransactionRemark(order, item));
        return change;
    }

    private String buildTransactionRemark(StockCountOrder order, StockCountItem item) {
        List<String> parts = new ArrayList<>();
        parts.add("盘点调账: " + order.getCountNo());
        if (StringUtils.isNotBlank(item.getDifferenceReason())) {
            parts.add("差异原因: " + item.getDifferenceReason().trim());
        }
        if (StringUtils.isNotBlank(item.getReviewRemark())) {
            parts.add("复盘备注: " + item.getReviewRemark().trim());
        } else if (StringUtils.isNotBlank(item.getRemark())) {
            parts.add("明细备注: " + item.getRemark().trim());
        } else if (StringUtils.isNotBlank(order.getRemark())) {
            parts.add("单据备注: " + order.getRemark().trim());
        }
        return String.join("；", parts);
    }

    private Map<Long, Location> loadLocationMap(Set<Long> locationIds) {
        if (locationIds == null || locationIds.isEmpty()) {
            return Map.of();
        }
        return locationMapper.selectByIds(locationIds).stream()
                .collect(Collectors.toMap(Location::getId, location -> location, (left, right) -> left));
    }

    private StockCountVo toStockCountVo(StockCountOrder order) {
        StockCountVo vo = MapstructUtils.convert(order, StockCountVo.class);
        if (vo != null) {
            normalizeHeader(vo);
        }
        return vo;
    }

    private void normalizeHeader(StockCountVo vo) {
        vo.setTotalItems(vo.getTotalItems() == null ? 0 : vo.getTotalItems());
        vo.setDifferenceItems(vo.getDifferenceItems() == null ? 0 : vo.getDifferenceItems());
        vo.setExpectedQty(normalizeQuantity(vo.getExpectedQty()));
        vo.setCountedQty(normalizeQuantity(vo.getCountedQty()));
        vo.setDifferenceQty(normalizeQuantity(vo.getDifferenceQty()));
    }

    private void fillHeaderDisplay(List<? extends StockCountVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        Set<Long> warehouseIds = records.stream()
                .map(StockCountVo::getWarehouseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Warehouse> warehouseMap = warehouseIds.isEmpty()
                ? Map.of()
                : warehouseMapper.selectByIds(warehouseIds).stream()
                .collect(Collectors.toMap(Warehouse::getId, warehouse -> warehouse, (left, right) -> left));
        for (StockCountVo record : records) {
            Warehouse warehouse = warehouseMap.get(record.getWarehouseId());
            if (warehouse != null) {
                record.setWarehouseCode(warehouse.getWarehouseCode());
                record.setWarehouseName(warehouse.getWarehouseName());
            }
        }
    }

    private void fillItemDisplay(List<StockCountItemVo> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Set<Long> areaIds = items.stream()
                .map(StockCountItemVo::getAreaId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> locationIds = items.stream()
                .map(StockCountItemVo::getLocationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> materialIds = items.stream()
                .map(StockCountItemVo::getMaterialId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Area> areaMap = areaIds.isEmpty()
                ? Map.of()
                : areaMapper.selectByIds(areaIds).stream()
                .collect(Collectors.toMap(Area::getId, area -> area, (left, right) -> left));
        Map<Long, Location> locationMap = locationIds.isEmpty()
                ? Map.of()
                : locationMapper.selectByIds(locationIds).stream()
                .collect(Collectors.toMap(Location::getId, location -> location, (left, right) -> left));
        Map<Long, Material> materialMap = materialIds.isEmpty()
                ? Map.of()
                : materialMapper.selectByIds(materialIds).stream()
                .collect(Collectors.toMap(Material::getId, material -> material, (left, right) -> left));

        for (StockCountItemVo item : items) {
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
            item.setExpectedQty(normalizeQuantity(item.getExpectedQty()));
            item.setCountedQty(normalizeQuantity(item.getCountedQty()));
            if (item.getReviewCountedQty() != null) {
                item.setReviewCountedQty(normalizeQuantity(item.getReviewCountedQty()));
            }
            item.setDifferenceQty(normalizeQuantity(item.getDifferenceQty()));
        }
    }

    private void updateRemark(StockCountOrder order, StockCountActionBo bo) {
        String remark = StringUtils.trimToNull(bo.getRemark());
        if (remark != null) {
            order.setRemark(remark);
        }
    }

    private void updateReviewAuditInfo(StockCountOrder order, StockCountActionBo bo, LocalDateTime reviewTime) {
        order.setReviewTime(reviewTime);
        order.setReviewBy(resolveOperator());
        order.setReviewRemark(StringUtils.defaultString(StringUtils.trimToNull(bo.getRemark())));
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

    private BigDecimal normalizeQuantity(BigDecimal quantity) {
        return quantity == null ? BigDecimal.ZERO : quantity;
    }

    private String normalizeBatchNo(String batchNo) {
        return StringUtils.defaultString(StringUtils.trimToNull(batchNo));
    }

    private String normalizeStatus(String status) {
        return StringUtils.trimToEmpty(status).toUpperCase(Locale.ROOT);
    }
}
