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
import com.berlin.aetherflow.wms.constant.BizCodeTypeConst;
import com.berlin.aetherflow.wms.constant.OrderStatusConst;
import com.berlin.aetherflow.wms.constant.OutboundAllocationStatusConst;
import com.berlin.aetherflow.wms.constant.PickingTaskStatusConst;
import com.berlin.aetherflow.wms.constant.WaveConstants;
import com.berlin.aetherflow.wms.domain.bo.PickingTaskActionBo;
import com.berlin.aetherflow.wms.domain.entity.Location;
import com.berlin.aetherflow.wms.domain.entity.Material;
import com.berlin.aetherflow.wms.domain.entity.OutboundAllocation;
import com.berlin.aetherflow.wms.domain.entity.OutboundOrder;
import com.berlin.aetherflow.wms.domain.entity.PickingTask;
import com.berlin.aetherflow.wms.domain.entity.PickingTaskItem;
import com.berlin.aetherflow.wms.domain.entity.Warehouse;
import com.berlin.aetherflow.wms.domain.entity.WaveOrder;
import com.berlin.aetherflow.wms.domain.entity.WaveOrderItem;
import com.berlin.aetherflow.wms.domain.query.PickingTaskQuery;
import com.berlin.aetherflow.wms.domain.vo.PickingTaskDetailVo;
import com.berlin.aetherflow.wms.domain.vo.PickingTaskItemVo;
import com.berlin.aetherflow.wms.domain.vo.PickingTaskVo;
import com.berlin.aetherflow.wms.mapper.LocationMapper;
import com.berlin.aetherflow.wms.mapper.MaterialMapper;
import com.berlin.aetherflow.wms.mapper.OutboundAllocationMapper;
import com.berlin.aetherflow.wms.mapper.OutboundOrderMapper;
import com.berlin.aetherflow.wms.mapper.PickingTaskMapper;
import com.berlin.aetherflow.wms.mapper.WarehouseMapper;
import com.berlin.aetherflow.wms.mapper.WaveOrderItemMapper;
import com.berlin.aetherflow.wms.mapper.WaveOrderMapper;
import com.berlin.aetherflow.wms.service.OutboundAllocationService;
import com.berlin.aetherflow.wms.service.PickingTaskItemService;
import com.berlin.aetherflow.wms.service.PickingTaskService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 拣货任务 Service 实现。
 */
@Service
@AllArgsConstructor
public class PickingTaskServiceImpl extends ServiceImpl<PickingTaskMapper, PickingTask>
        implements PickingTaskService {

    private static final String SOURCE_TYPE_OUTBOUND_ORDER = "OUTBOUND_ORDER";

    private static final String SOURCE_TYPE_WAVE = "WAVE";

    private final PickingTaskMapper pickingTaskMapper;
    private final PickingTaskItemService pickingTaskItemService;
    private final OutboundOrderMapper outboundOrderMapper;
    private final OutboundAllocationMapper outboundAllocationMapper;
    private final OutboundAllocationService outboundAllocationService;
    private final WaveOrderMapper waveOrderMapper;
    private final WaveOrderItemMapper waveOrderItemMapper;
    private final WarehouseMapper warehouseMapper;
    private final LocationMapper locationMapper;
    private final MaterialMapper materialMapper;

    @Override
    public PageResult<PickingTaskVo> queryList(PickingTaskQuery query) {
        LambdaQueryWrapper<PickingTask> lqw = Wrappers.<PickingTask>lambdaQuery()
                .like(StringUtils.isNotBlank(query.getTaskNo()), PickingTask::getTaskNo, query.getTaskNo())
                .eq(StringUtils.isNotBlank(query.getSourceType()), PickingTask::getSourceType, normalizeSourceType(query.getSourceType()))
                .eq(query.getWaveId() != null, PickingTask::getWaveId, query.getWaveId())
                .like(StringUtils.isNotBlank(query.getWaveNo()), PickingTask::getWaveNo, query.getWaveNo())
                .eq(query.getOutboundOrderId() != null, PickingTask::getOutboundOrderId, query.getOutboundOrderId())
                .like(StringUtils.isNotBlank(query.getOutboundOrderNo()), PickingTask::getOutboundOrderNo, query.getOutboundOrderNo())
                .eq(query.getWarehouseId() != null, PickingTask::getWarehouseId, query.getWarehouseId())
                .eq(StringUtils.isNotBlank(query.getStatus()), PickingTask::getStatus, normalizeStatus(query.getStatus()));

        Set<Long> matchedTaskIds = findMatchedTaskIds(query);
        if (matchedTaskIds != null) {
            if (matchedTaskIds.isEmpty()) {
                return PageResult.of(Long.valueOf(query.getPageNo()), Long.valueOf(query.getPageSize()), 0L, 0L, List.of());
            }
            lqw.in(PickingTask::getId, matchedTaskIds);
        }

        IPage<PickingTask> page = new Page<>(query.getPageNo(), query.getPageSize());
        if (StringUtils.isBlank(query.getSortBy()) || query.getIsAsc() == null) {
            lqw.orderByDesc(PickingTask::getCreateTime);
        }
        OrderUtil.addOrder(page, query.getSortBy(), query.getIsAsc());

        IPage<PickingTask> result = pickingTaskMapper.selectPage(page, lqw);
        List<PickingTaskVo> records = result.getRecords().stream()
                .map(this::toTaskVo)
                .filter(Objects::nonNull)
                .toList();
        fillWarehouseDisplay(records);
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    @Override
    public PickingTaskDetailVo getDetailById(Long id) {
        PickingTask task = getById(id);
        if (task == null) {
            return null;
        }

        PickingTaskDetailVo detailVo = MapstructUtils.convert(task, PickingTaskDetailVo.class);
        if (detailVo == null) {
            return null;
        }
        normalizeHeader(detailVo);
        fillWarehouseDisplay(List.of(detailVo));

        List<PickingTaskItem> items = pickingTaskItemService.lambdaQuery()
                .eq(PickingTaskItem::getTaskId, id)
                .orderByAsc(PickingTaskItem::getLineNo, PickingTaskItem::getId)
                .list();
        List<PickingTaskItemVo> itemVos = items.stream()
                .map(item -> MapstructUtils.convert(item, PickingTaskItemVo.class))
                .filter(Objects::nonNull)
                .toList();
        fillItemDisplay(itemVos);
        detailVo.setItems(itemVos);
        return detailVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFromOutboundOrder(Long outboundOrderId) {
        OutboundOrder order = outboundOrderMapper.selectById(outboundOrderId);
        if (order == null) {
            throw new RuntimeException("出库单不存在");
        }
        if (!OrderStatusConst.DRAFT.equals(order.getStatus())) {
            throw new RuntimeException("仅草稿出库单允许生成拣货任务");
        }

        PickingTask existingTask = getOne(Wrappers.<PickingTask>lambdaQuery()
                .eq(PickingTask::getOutboundOrderId, outboundOrderId)
                .last("limit 1"));
        if (existingTask != null && !PickingTaskStatusConst.CANCELLED.equals(existingTask.getStatus())) {
            backfillOutboundOrderSource(existingTask);
            return existingTask.getId();
        }
        ensureNoActiveWavePickingTask(outboundOrderId);

        if (!outboundAllocationService.hasActiveAllocation(outboundOrderId)) {
            outboundAllocationService.allocate(outboundOrderId);
        }
        List<OutboundAllocation> allocations = outboundAllocationMapper.selectList(Wrappers.<OutboundAllocation>lambdaQuery()
                .eq(OutboundAllocation::getOrderId, outboundOrderId)
                .eq(OutboundAllocation::getStatus, OutboundAllocationStatusConst.ACTIVE)
                .orderByAsc(OutboundAllocation::getLineNo, OutboundAllocation::getId));
        if (allocations.isEmpty()) {
            throw new RuntimeException("出库单没有可拣货的库存分配");
        }

        BigDecimal totalQty = allocations.stream()
                .map(OutboundAllocation::getAllocatedQty)
                .map(this::normalizeQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PickingTask task;
        if (existingTask == null) {
            task = new PickingTask();
            task.setTaskNo(CodeGenerate.generateSimple(BizCodeTypeConst.PICKING_TASK));
            task.setSourceType(SOURCE_TYPE_OUTBOUND_ORDER);
            task.setOutboundOrderId(order.getId());
            task.setOutboundOrderNo(order.getOrderNo());
            task.setWaveNo("");
            task.setWarehouseId(order.getWarehouseId());
            task.setStatus(PickingTaskStatusConst.PENDING);
            task.setTotalQty(totalQty);
            task.setPickedQty(BigDecimal.ZERO);
            task.setRemark(StringUtils.defaultString(order.getRemark()));
            if (!save(task)) {
                throw new RuntimeException("拣货任务创建失败");
            }
        } else {
            task = existingTask;
            task.setSourceType(SOURCE_TYPE_OUTBOUND_ORDER);
            task.setOutboundOrderNo(order.getOrderNo());
            task.setWaveNo("");
            task.setWarehouseId(order.getWarehouseId());
            task.setStatus(PickingTaskStatusConst.PENDING);
            task.setTotalQty(totalQty);
            task.setPickedQty(BigDecimal.ZERO);
            task.setPickingTime(null);
            task.setRemark(StringUtils.defaultString(order.getRemark()));
            if (!updateById(task)) {
                throw new RuntimeException("拣货任务重建失败");
            }
            pickingTaskItemService.remove(Wrappers.<PickingTaskItem>lambdaQuery()
                    .eq(PickingTaskItem::getTaskId, task.getId()));
        }

        List<PickingTaskItem> taskItems = new ArrayList<>(allocations.size());
        for (OutboundAllocation allocation : allocations) {
            PickingTaskItem taskItem = new PickingTaskItem();
            taskItem.setTaskId(task.getId());
            taskItem.setOutboundOrderItemId(allocation.getOrderItemId());
            taskItem.setAllocationId(allocation.getId());
            taskItem.setInventoryId(allocation.getInventoryId());
            taskItem.setLineNo(allocation.getLineNo());
            taskItem.setWarehouseId(allocation.getWarehouseId());
            taskItem.setLocationId(allocation.getLocationId());
            taskItem.setMaterialId(allocation.getMaterialId());
            taskItem.setBatchNo(normalizeBatchNo(allocation.getBatchNo()));
            taskItem.setProductionDate(allocation.getProductionDate());
            taskItem.setExpiryDate(allocation.getExpiryDate());
            taskItem.setPlannedQty(normalizeQuantity(allocation.getAllocatedQty()));
            taskItem.setPickedQty(BigDecimal.ZERO);
            taskItem.setStatus(PickingTaskStatusConst.PENDING);
            taskItem.setRemark(StringUtils.defaultString(allocation.getRemark()));
            taskItems.add(taskItem);
        }
        if (!pickingTaskItemService.saveBatch(taskItems)) {
            throw new RuntimeException("拣货任务明细创建失败");
        }
        return task.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFromWave(Long waveId) {
        WaveOrder wave = loadReleasedWave(waveId);
        PickingTask existingActiveTask = getOne(Wrappers.<PickingTask>lambdaQuery()
                .eq(PickingTask::getWaveId, waveId)
                .ne(PickingTask::getStatus, PickingTaskStatusConst.CANCELLED)
                .last("limit 1"));
        if (existingActiveTask != null) {
            return existingActiveTask.getId();
        }
        PickingTask existingTask = getOne(Wrappers.<PickingTask>lambdaQuery()
                .eq(PickingTask::getWaveId, waveId)
                .eq(PickingTask::getStatus, PickingTaskStatusConst.CANCELLED)
                .last("limit 1"));

        List<WaveOrderItem> waveItems = loadWaveItems(waveId);
        List<Long> orderIds = extractWaveOrderIds(waveItems);
        ensureNoActiveOutboundOrderPickingTask(orderIds);
        ensureActiveAllocations(orderIds);

        List<OutboundAllocation> allocations = outboundAllocationMapper.selectList(Wrappers.<OutboundAllocation>lambdaQuery()
                .in(OutboundAllocation::getOrderId, orderIds)
                .eq(OutboundAllocation::getStatus, OutboundAllocationStatusConst.ACTIVE)
                .orderByAsc(OutboundAllocation::getOrderId, OutboundAllocation::getLineNo, OutboundAllocation::getId));
        ensureAllWaveOrdersAllocated(waveItems, allocations);

        Map<Long, WaveOrderItem> waveItemMap = waveItems.stream()
                .filter(item -> item.getOutboundOrderItemId() != null)
                .collect(Collectors.toMap(WaveOrderItem::getOutboundOrderItemId, item -> item, (left, right) -> left));
        for (OutboundAllocation allocation : allocations) {
            if (allocation.getOrderItemId() == null || !waveItemMap.containsKey(allocation.getOrderItemId())) {
                throw new RuntimeException("波次明细与库存分配不一致，请重新规划波次");
            }
        }

        List<OutboundAllocation> sortedAllocations = new ArrayList<>(allocations);
        sortedAllocations.sort(Comparator
                .comparing((OutboundAllocation allocation) -> waveItemMap.get(allocation.getOrderItemId()).getLineNo(),
                        Comparator.nullsLast(Integer::compareTo))
                .thenComparing(OutboundAllocation::getId, Comparator.nullsLast(Long::compareTo)));

        BigDecimal totalQty = sortedAllocations.stream()
                .map(OutboundAllocation::getAllocatedQty)
                .map(this::normalizeQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        PickingTask task;
        if (existingTask == null) {
            task = new PickingTask();
            task.setTaskNo(CodeGenerate.generateSimple(BizCodeTypeConst.PICKING_TASK));
            applyWaveTaskHeader(task, wave, totalQty);
            if (!save(task)) {
                throw new RuntimeException("波次拣货任务创建失败");
            }
        } else {
            task = existingTask;
            applyWaveTaskHeader(task, wave, totalQty);
            if (!updateById(task)) {
                throw new RuntimeException("波次拣货任务重建失败");
            }
            pickingTaskItemService.remove(Wrappers.<PickingTaskItem>lambdaQuery()
                    .eq(PickingTaskItem::getTaskId, task.getId()));
        }

        List<PickingTaskItem> taskItems = new ArrayList<>(sortedAllocations.size());
        for (OutboundAllocation allocation : sortedAllocations) {
            WaveOrderItem waveItem = waveItemMap.get(allocation.getOrderItemId());
            PickingTaskItem taskItem = new PickingTaskItem();
            taskItem.setTaskId(task.getId());
            taskItem.setWaveId(wave.getId());
            taskItem.setWaveItemId(waveItem.getId());
            taskItem.setOutboundOrderItemId(allocation.getOrderItemId());
            taskItem.setAllocationId(allocation.getId());
            taskItem.setInventoryId(allocation.getInventoryId());
            taskItem.setLineNo(taskItems.size() + 1);
            taskItem.setWarehouseId(allocation.getWarehouseId());
            taskItem.setLocationId(allocation.getLocationId());
            taskItem.setMaterialId(allocation.getMaterialId());
            taskItem.setBatchNo(normalizeBatchNo(allocation.getBatchNo()));
            taskItem.setProductionDate(allocation.getProductionDate());
            taskItem.setExpiryDate(allocation.getExpiryDate());
            taskItem.setPlannedQty(normalizeQuantity(allocation.getAllocatedQty()));
            taskItem.setPickedQty(BigDecimal.ZERO);
            taskItem.setStatus(PickingTaskStatusConst.PENDING);
            taskItem.setRemark(StringUtils.defaultString(allocation.getRemark()));
            taskItems.add(taskItem);
        }
        if (!pickingTaskItemService.saveBatch(taskItems)) {
            throw new RuntimeException("波次拣货任务明细创建失败");
        }
        return task.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean applyAction(Long id, PickingTaskActionBo bo) {
        if (id == null) {
            throw new RuntimeException("拣货任务ID不能为空");
        }
        PickingTask task = getById(id);
        if (task == null) {
            throw new RuntimeException("拣货任务不存在");
        }
        if (!PickingTaskStatusConst.PENDING.equals(task.getStatus())) {
            throw new RuntimeException("当前状态不可操作");
        }

        String action = StringUtils.trimToEmpty(bo.getAction()).toUpperCase(Locale.ROOT);
        if ("COMPLETE".equals(action)) {
            completeTask(task, bo);
            return true;
        }
        if ("CANCEL".equals(action)) {
            cancelTask(task, bo);
            return true;
        }
        throw new RuntimeException("不支持的动作: " + bo.getAction());
    }

    private WaveOrder loadReleasedWave(Long waveId) {
        if (waveId == null) {
            throw new RuntimeException("波次ID不能为空");
        }
        WaveOrder wave = waveOrderMapper.selectById(waveId);
        if (wave == null) {
            throw new RuntimeException("波次不存在");
        }
        if (WaveConstants.STATUS_DRAFT.equals(wave.getStatus())) {
            throw new RuntimeException("请先发布波次后再生成拣货任务");
        }
        if (WaveConstants.STATUS_CANCELLED.equals(wave.getStatus())) {
            throw new RuntimeException("已取消波次不允许生成拣货任务");
        }
        if (!WaveConstants.STATUS_RELEASED.equals(wave.getStatus())) {
            throw new RuntimeException("当前波次状态不可生成拣货任务");
        }
        return wave;
    }

    private List<WaveOrderItem> loadWaveItems(Long waveId) {
        List<WaveOrderItem> waveItems = waveOrderItemMapper.selectList(Wrappers.<WaveOrderItem>lambdaQuery()
                .eq(WaveOrderItem::getWaveId, waveId)
                .orderByAsc(WaveOrderItem::getLineNo, WaveOrderItem::getId));
        if (waveItems == null || waveItems.isEmpty()) {
            throw new RuntimeException("波次明细不能为空");
        }
        return waveItems;
    }

    private List<Long> extractWaveOrderIds(List<WaveOrderItem> waveItems) {
        LinkedHashSet<Long> orderIds = waveItems.stream()
                .map(WaveOrderItem::getOutboundOrderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (orderIds.isEmpty()) {
            throw new RuntimeException("波次出库单不能为空");
        }
        return new ArrayList<>(orderIds);
    }

    private void ensureActiveAllocations(List<Long> orderIds) {
        for (Long orderId : orderIds) {
            if (!outboundAllocationService.hasActiveAllocation(orderId)) {
                outboundAllocationService.allocate(orderId);
            }
        }
    }

    private void ensureNoActiveOutboundOrderPickingTask(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return;
        }
        PickingTask task = getOne(Wrappers.<PickingTask>lambdaQuery()
                .in(PickingTask::getOutboundOrderId, orderIds)
                .ne(PickingTask::getStatus, PickingTaskStatusConst.CANCELLED)
                .last("limit 1"));
        if (task != null) {
            String orderNo = StringUtils.defaultIfBlank(task.getOutboundOrderNo(), String.valueOf(task.getOutboundOrderId()));
            throw new RuntimeException("出库单已生成单出库拣货任务，请先取消后再生成波次拣货任务: " + orderNo);
        }
    }

    private void ensureNoActiveWavePickingTask(Long outboundOrderId) {
        if (outboundOrderId == null) {
            return;
        }
        PickingTask task = getOne(Wrappers.<PickingTask>lambdaQuery()
                .inSql(PickingTask::getWaveId,
                        "select distinct wave_id from wave_order_item where outbound_order_id = " + outboundOrderId)
                .ne(PickingTask::getStatus, PickingTaskStatusConst.CANCELLED)
                .last("limit 1"));
        if (task != null) {
            String waveNo = StringUtils.defaultIfBlank(task.getWaveNo(), String.valueOf(task.getWaveId()));
            throw new RuntimeException("出库单已生成波次拣货任务，请先取消后再生成单出库拣货任务: " + waveNo);
        }
    }

    private void ensureAllWaveOrdersAllocated(List<WaveOrderItem> waveItems, List<OutboundAllocation> allocations) {
        if (allocations == null || allocations.isEmpty()) {
            throw new RuntimeException("波次没有可拣货的库存分配");
        }

        Set<Long> allocatedOrderIds = allocations.stream()
                .map(OutboundAllocation::getOrderId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> allocatedItemIds = allocations.stream()
                .map(OutboundAllocation::getOrderItemId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (WaveOrderItem waveItem : waveItems) {
            if (waveItem.getOutboundOrderId() != null && !allocatedOrderIds.contains(waveItem.getOutboundOrderId())) {
                throw new RuntimeException("出库单没有可拣货的库存分配: "
                        + StringUtils.defaultString(waveItem.getOutboundOrderNo(), String.valueOf(waveItem.getOutboundOrderId())));
            }
            if (waveItem.getOutboundOrderItemId() != null && !allocatedItemIds.contains(waveItem.getOutboundOrderItemId())) {
                throw new RuntimeException("波次明细没有可拣货的库存分配，行号: " + waveItem.getLineNo());
            }
        }
    }

    private void applyWaveTaskHeader(PickingTask task, WaveOrder wave, BigDecimal totalQty) {
        task.setSourceType(SOURCE_TYPE_WAVE);
        task.setWaveId(wave.getId());
        task.setWaveNo(StringUtils.defaultString(wave.getWaveNo()));
        task.setOutboundOrderId(null);
        task.setOutboundOrderNo(StringUtils.defaultString(wave.getWaveNo()));
        task.setWarehouseId(wave.getWarehouseId());
        task.setStatus(PickingTaskStatusConst.PENDING);
        task.setTotalQty(normalizeQuantity(totalQty));
        task.setPickedQty(BigDecimal.ZERO);
        task.setPickingTime(null);
        task.setRemark(StringUtils.defaultString(wave.getRemark()));
    }

    private void backfillOutboundOrderSource(PickingTask task) {
        if (StringUtils.isNotBlank(task.getSourceType())) {
            return;
        }
        task.setSourceType(SOURCE_TYPE_OUTBOUND_ORDER);
        updateById(task);
    }

    private Set<Long> findMatchedTaskIds(PickingTaskQuery query) {
        boolean hasItemFilter = query.getLocationId() != null
                || query.getMaterialId() != null
                || StringUtils.isNotBlank(query.getBatchNo());
        if (!hasItemFilter) {
            return null;
        }

        LambdaQueryWrapper<PickingTaskItem> itemLqw = Wrappers.<PickingTaskItem>lambdaQuery()
                .select(PickingTaskItem::getTaskId)
                .eq(query.getLocationId() != null, PickingTaskItem::getLocationId, query.getLocationId())
                .eq(query.getMaterialId() != null, PickingTaskItem::getMaterialId, query.getMaterialId())
                .like(StringUtils.isNotBlank(query.getBatchNo()), PickingTaskItem::getBatchNo, query.getBatchNo());
        return pickingTaskItemService.list(itemLqw).stream()
                .map(PickingTaskItem::getTaskId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private void completeTask(PickingTask task, PickingTaskActionBo bo) {
        List<PickingTaskItem> items = loadTaskItems(task.getId());
        LocalDateTime now = LocalDateTime.now();
        for (PickingTaskItem item : items) {
            item.setPickedQty(normalizeQuantity(item.getPlannedQty()));
            item.setStatus(PickingTaskStatusConst.COMPLETED);
        }
        if (!pickingTaskItemService.updateBatchById(items)) {
            throw new RuntimeException("拣货任务明细更新失败");
        }

        task.setPickedQty(normalizeQuantity(task.getTotalQty()));
        task.setStatus(PickingTaskStatusConst.COMPLETED);
        task.setPickingTime(now);
        updateRemark(task, bo);
        if (!updateById(task)) {
            throw new RuntimeException("拣货任务更新失败");
        }
    }

    private void cancelTask(PickingTask task, PickingTaskActionBo bo) {
        List<PickingTaskItem> items = loadTaskItems(task.getId());
        for (PickingTaskItem item : items) {
            item.setStatus(PickingTaskStatusConst.CANCELLED);
        }
        if (!pickingTaskItemService.updateBatchById(items)) {
            throw new RuntimeException("拣货任务明细更新失败");
        }

        task.setStatus(PickingTaskStatusConst.CANCELLED);
        updateRemark(task, bo);
        if (!updateById(task)) {
            throw new RuntimeException("拣货任务更新失败");
        }
    }

    private List<PickingTaskItem> loadTaskItems(Long taskId) {
        List<PickingTaskItem> items = pickingTaskItemService.lambdaQuery()
                .eq(PickingTaskItem::getTaskId, taskId)
                .orderByAsc(PickingTaskItem::getLineNo, PickingTaskItem::getId)
                .list();
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("拣货任务明细不能为空");
        }
        return items;
    }

    private PickingTaskVo toTaskVo(PickingTask task) {
        PickingTaskVo vo = MapstructUtils.convert(task, PickingTaskVo.class);
        if (vo != null) {
            normalizeHeader(vo);
        }
        return vo;
    }

    private void normalizeHeader(PickingTaskVo vo) {
        vo.setTotalQty(normalizeQuantity(vo.getTotalQty()));
        vo.setPickedQty(normalizeQuantity(vo.getPickedQty()));
    }

    private void fillWarehouseDisplay(List<? extends PickingTaskVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        Set<Long> warehouseIds = records.stream()
                .map(PickingTaskVo::getWarehouseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (warehouseIds.isEmpty()) {
            return;
        }
        Map<Long, Warehouse> warehouseMap = warehouseMapper.selectByIds(warehouseIds).stream()
                .collect(Collectors.toMap(Warehouse::getId, warehouse -> warehouse, (left, right) -> left));
        for (PickingTaskVo record : records) {
            Warehouse warehouse = warehouseMap.get(record.getWarehouseId());
            if (warehouse != null) {
                record.setWarehouseCode(warehouse.getWarehouseCode());
                record.setWarehouseName(warehouse.getWarehouseName());
            }
        }
    }

    private void fillItemDisplay(List<PickingTaskItemVo> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        Set<Long> materialIds = items.stream()
                .map(PickingTaskItemVo::getMaterialId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> locationIds = items.stream()
                .map(PickingTaskItemVo::getLocationId)
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

        for (PickingTaskItemVo item : items) {
            Material material = materialMap.get(item.getMaterialId());
            if (material != null) {
                item.setMaterialCode(material.getMaterialCode());
                item.setMaterialName(material.getMaterialName());
            }
            Location location = locationMap.get(item.getLocationId());
            if (location != null) {
                item.setLocationCode(location.getLocationCode());
                item.setLocationName(location.getLocationName());
            }
            item.setPlannedQty(normalizeQuantity(item.getPlannedQty()));
            item.setPickedQty(normalizeQuantity(item.getPickedQty()));
        }
    }

    private void updateRemark(PickingTask task, PickingTaskActionBo bo) {
        String remark = StringUtils.trimToNull(bo.getRemark());
        if (remark != null) {
            task.setRemark(remark);
        }
    }

    private BigDecimal normalizeQuantity(BigDecimal quantity) {
        return quantity == null ? BigDecimal.ZERO : quantity;
    }

    private String normalizeStatus(String status) {
        return StringUtils.trimToEmpty(status).toUpperCase(Locale.ROOT);
    }

    private String normalizeSourceType(String sourceType) {
        return StringUtils.trimToEmpty(sourceType).toUpperCase(Locale.ROOT);
    }

    private String normalizeBatchNo(String batchNo) {
        return StringUtils.defaultString(StringUtils.trimToNull(batchNo));
    }
}
