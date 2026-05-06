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
import com.berlin.aetherflow.wms.constant.PutawayTaskStatusConst;
import com.berlin.aetherflow.wms.domain.bo.PutawayTaskActionBo;
import com.berlin.aetherflow.wms.domain.entity.InboundOrder;
import com.berlin.aetherflow.wms.domain.entity.InboundOrderItem;
import com.berlin.aetherflow.wms.domain.entity.Location;
import com.berlin.aetherflow.wms.domain.entity.Material;
import com.berlin.aetherflow.wms.domain.entity.PutawayTask;
import com.berlin.aetherflow.wms.domain.entity.PutawayTaskItem;
import com.berlin.aetherflow.wms.domain.entity.Warehouse;
import com.berlin.aetherflow.wms.domain.query.PutawayTaskQuery;
import com.berlin.aetherflow.wms.domain.vo.PutawayTaskDetailVo;
import com.berlin.aetherflow.wms.domain.vo.PutawayTaskItemVo;
import com.berlin.aetherflow.wms.domain.vo.PutawayTaskVo;
import com.berlin.aetherflow.wms.mapper.LocationMapper;
import com.berlin.aetherflow.wms.mapper.MaterialMapper;
import com.berlin.aetherflow.wms.mapper.PutawayTaskMapper;
import com.berlin.aetherflow.wms.mapper.WarehouseMapper;
import com.berlin.aetherflow.wms.service.PutawayTaskItemService;
import com.berlin.aetherflow.wms.service.PutawayTaskService;
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
 * 上架任务 Service 实现。
 */
@Service
@AllArgsConstructor
public class PutawayTaskServiceImpl extends ServiceImpl<PutawayTaskMapper, PutawayTask>
        implements PutawayTaskService {

    private final PutawayTaskMapper putawayTaskMapper;
    private final PutawayTaskItemService putawayTaskItemService;
    private final WarehouseMapper warehouseMapper;
    private final LocationMapper locationMapper;
    private final MaterialMapper materialMapper;

    @Override
    public PageResult<PutawayTaskVo> queryList(PutawayTaskQuery query) {
        LambdaQueryWrapper<PutawayTask> lqw = Wrappers.<PutawayTask>lambdaQuery()
                .like(StringUtils.isNotBlank(query.getTaskNo()), PutawayTask::getTaskNo, query.getTaskNo())
                .eq(query.getInboundOrderId() != null, PutawayTask::getInboundOrderId, query.getInboundOrderId())
                .like(StringUtils.isNotBlank(query.getInboundOrderNo()), PutawayTask::getInboundOrderNo, query.getInboundOrderNo())
                .eq(query.getWarehouseId() != null, PutawayTask::getWarehouseId, query.getWarehouseId())
                .eq(StringUtils.isNotBlank(query.getStatus()), PutawayTask::getStatus, normalizeStatus(query.getStatus()));

        Set<Long> matchedTaskIds = findMatchedTaskIds(query);
        if (matchedTaskIds != null) {
            if (matchedTaskIds.isEmpty()) {
                return PageResult.of(Long.valueOf(query.getPageNo()), Long.valueOf(query.getPageSize()), 0L, 0L, List.of());
            }
            lqw.in(PutawayTask::getId, matchedTaskIds);
        }

        IPage<PutawayTask> page = new Page<>(query.getPageNo(), query.getPageSize());
        if (StringUtils.isBlank(query.getSortBy()) || query.getIsAsc() == null) {
            lqw.orderByDesc(PutawayTask::getCreateTime);
        }
        OrderUtil.addOrder(page, query.getSortBy(), query.getIsAsc());

        IPage<PutawayTask> result = putawayTaskMapper.selectPage(page, lqw);
        List<PutawayTaskVo> records = result.getRecords().stream()
                .map(this::toTaskVo)
                .filter(Objects::nonNull)
                .toList();
        fillWarehouseDisplay(records);
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    @Override
    public PutawayTaskDetailVo getDetailById(Long id) {
        PutawayTask task = getById(id);
        if (task == null) {
            return null;
        }

        PutawayTaskDetailVo detailVo = MapstructUtils.convert(task, PutawayTaskDetailVo.class);
        if (detailVo == null) {
            return null;
        }
        fillWarehouseDisplay(List.of(detailVo));

        List<PutawayTaskItem> items = putawayTaskItemService.lambdaQuery()
                .eq(PutawayTaskItem::getTaskId, id)
                .orderByAsc(PutawayTaskItem::getLineNo)
                .list();
        List<PutawayTaskItemVo> itemVos = items.stream()
                .map(item -> MapstructUtils.convert(item, PutawayTaskItemVo.class))
                .filter(Objects::nonNull)
                .toList();
        fillItemDisplay(itemVos);
        detailVo.setItems(itemVos);
        return detailVo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createFromInboundOrder(InboundOrder order, List<InboundOrderItem> orderItems) {
        if (order == null || order.getId() == null || orderItems == null || orderItems.isEmpty()) {
            return;
        }
        Long existingCount = count(Wrappers.<PutawayTask>lambdaQuery()
                .eq(PutawayTask::getInboundOrderId, order.getId()));
        if (existingCount != null && existingCount > 0) {
            return;
        }

        List<PutawayTaskItem> taskItems = new ArrayList<>(orderItems.size());
        BigDecimal totalQty = BigDecimal.ZERO;
        for (InboundOrderItem orderItem : orderItems) {
            BigDecimal plannedQty = resolvePutawayQty(orderItem);
            totalQty = totalQty.add(plannedQty);

            PutawayTaskItem taskItem = new PutawayTaskItem();
            taskItem.setInboundOrderItemId(orderItem.getId());
            taskItem.setLineNo(orderItem.getLineNo());
            taskItem.setMaterialId(orderItem.getMaterialId());
            taskItem.setLocationId(orderItem.getLocationId());
            taskItem.setBatchNo(normalizeBatchNo(orderItem.getBatchNo()));
            taskItem.setProductionDate(orderItem.getProductionDate());
            taskItem.setExpiryDate(orderItem.getExpiryDate());
            taskItem.setPlannedQty(plannedQty);
            taskItem.setCompletedQty(BigDecimal.ZERO);
            taskItem.setStatus(PutawayTaskStatusConst.PENDING);
            taskItem.setRemark(StringUtils.defaultString(orderItem.getRemark()));
            taskItems.add(taskItem);
        }

        PutawayTask task = new PutawayTask();
        task.setTaskNo(CodeGenerate.generateSimple(BizCodeTypeConst.PUTAWAY_TASK));
        task.setInboundOrderId(order.getId());
        task.setInboundOrderNo(order.getOrderNo());
        task.setWarehouseId(order.getWarehouseId());
        task.setStatus(PutawayTaskStatusConst.PENDING);
        task.setTotalQty(totalQty);
        task.setCompletedQty(BigDecimal.ZERO);
        task.setRemark(StringUtils.defaultString(order.getRemark()));
        boolean saved = save(task);
        if (!saved) {
            throw new RuntimeException("上架任务创建失败");
        }

        taskItems.forEach(item -> item.setTaskId(task.getId()));
        boolean itemsSaved = putawayTaskItemService.saveBatch(taskItems);
        if (!itemsSaved) {
            throw new RuntimeException("上架任务明细创建失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean applyAction(Long id, PutawayTaskActionBo bo) {
        if (id == null) {
            throw new RuntimeException("上架任务ID不能为空");
        }
        PutawayTask task = getById(id);
        if (task == null) {
            throw new RuntimeException("上架任务不存在");
        }
        if (!PutawayTaskStatusConst.PENDING.equals(task.getStatus())) {
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

    private Set<Long> findMatchedTaskIds(PutawayTaskQuery query) {
        boolean hasItemFilter = query.getLocationId() != null
                || query.getMaterialId() != null
                || StringUtils.isNotBlank(query.getBatchNo());
        if (!hasItemFilter) {
            return null;
        }

        LambdaQueryWrapper<PutawayTaskItem> itemLqw = Wrappers.<PutawayTaskItem>lambdaQuery()
                .select(PutawayTaskItem::getTaskId)
                .eq(query.getLocationId() != null, PutawayTaskItem::getLocationId, query.getLocationId())
                .eq(query.getMaterialId() != null, PutawayTaskItem::getMaterialId, query.getMaterialId())
                .like(StringUtils.isNotBlank(query.getBatchNo()), PutawayTaskItem::getBatchNo, query.getBatchNo());
        return putawayTaskItemService.list(itemLqw).stream()
                .map(PutawayTaskItem::getTaskId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private void completeTask(PutawayTask task, PutawayTaskActionBo bo) {
        List<PutawayTaskItem> items = loadTaskItems(task.getId());
        LocalDateTime now = LocalDateTime.now();
        for (PutawayTaskItem item : items) {
            item.setCompletedQty(normalizeQuantity(item.getPlannedQty()));
            item.setStatus(PutawayTaskStatusConst.COMPLETED);
        }
        boolean itemsUpdated = putawayTaskItemService.updateBatchById(items);
        if (!itemsUpdated) {
            throw new RuntimeException("上架任务明细更新失败");
        }

        task.setCompletedQty(normalizeQuantity(task.getTotalQty()));
        task.setStatus(PutawayTaskStatusConst.COMPLETED);
        task.setPutawayTime(now);
        updateRemark(task, bo);
        if (!updateById(task)) {
            throw new RuntimeException("上架任务更新失败");
        }
    }

    private void cancelTask(PutawayTask task, PutawayTaskActionBo bo) {
        List<PutawayTaskItem> items = loadTaskItems(task.getId());
        for (PutawayTaskItem item : items) {
            item.setStatus(PutawayTaskStatusConst.CANCELLED);
        }
        boolean itemsUpdated = putawayTaskItemService.updateBatchById(items);
        if (!itemsUpdated) {
            throw new RuntimeException("上架任务明细更新失败");
        }

        task.setStatus(PutawayTaskStatusConst.CANCELLED);
        updateRemark(task, bo);
        if (!updateById(task)) {
            throw new RuntimeException("上架任务更新失败");
        }
    }

    private List<PutawayTaskItem> loadTaskItems(Long taskId) {
        List<PutawayTaskItem> items = putawayTaskItemService.lambdaQuery()
                .eq(PutawayTaskItem::getTaskId, taskId)
                .orderByAsc(PutawayTaskItem::getLineNo)
                .list();
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("上架任务明细不能为空");
        }
        return items;
    }

    private BigDecimal resolvePutawayQty(InboundOrderItem item) {
        BigDecimal plannedQty = item.getReceivedQty();
        if (plannedQty == null || plannedQty.compareTo(BigDecimal.ZERO) <= 0) {
            plannedQty = item.getPlannedQty();
        }
        return normalizeQuantity(plannedQty);
    }

    private PutawayTaskVo toTaskVo(PutawayTask task) {
        PutawayTaskVo vo = MapstructUtils.convert(task, PutawayTaskVo.class);
        if (vo == null) {
            return null;
        }
        vo.setTotalQty(normalizeQuantity(vo.getTotalQty()));
        vo.setCompletedQty(normalizeQuantity(vo.getCompletedQty()));
        return vo;
    }

    private void fillWarehouseDisplay(List<? extends PutawayTaskVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        Set<Long> warehouseIds = records.stream()
                .map(PutawayTaskVo::getWarehouseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (warehouseIds.isEmpty()) {
            return;
        }
        Map<Long, Warehouse> warehouseMap = warehouseMapper.selectByIds(warehouseIds).stream()
                .collect(Collectors.toMap(Warehouse::getId, warehouse -> warehouse, (left, right) -> left));
        for (PutawayTaskVo record : records) {
            Warehouse warehouse = warehouseMap.get(record.getWarehouseId());
            if (warehouse != null) {
                record.setWarehouseCode(warehouse.getWarehouseCode());
                record.setWarehouseName(warehouse.getWarehouseName());
            }
        }
    }

    private void fillItemDisplay(List<PutawayTaskItemVo> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        Set<Long> materialIds = items.stream()
                .map(PutawayTaskItemVo::getMaterialId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> locationIds = items.stream()
                .map(PutawayTaskItemVo::getLocationId)
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

        for (PutawayTaskItemVo item : items) {
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
            item.setCompletedQty(normalizeQuantity(item.getCompletedQty()));
        }
    }

    private void updateRemark(PutawayTask task, PutawayTaskActionBo bo) {
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

    private String normalizeBatchNo(String batchNo) {
        return StringUtils.defaultString(StringUtils.trimToNull(batchNo));
    }
}
