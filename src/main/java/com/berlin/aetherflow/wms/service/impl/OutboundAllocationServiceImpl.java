package com.berlin.aetherflow.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import cn.dev33.satoken.stp.StpUtil;
import com.berlin.aetherflow.wms.constant.OrderStatusConst;
import com.berlin.aetherflow.wms.constant.OutboundAllocationStatusConst;
import com.berlin.aetherflow.wms.constant.PickingTaskStatusConst;
import com.berlin.aetherflow.wms.constant.StockBizTypeConst;
import com.berlin.aetherflow.wms.domain.bo.StockChangeBo;
import com.berlin.aetherflow.wms.domain.entity.Inventory;
import com.berlin.aetherflow.wms.domain.entity.Location;
import com.berlin.aetherflow.wms.domain.entity.Material;
import com.berlin.aetherflow.wms.domain.entity.OutboundAllocation;
import com.berlin.aetherflow.wms.domain.entity.OutboundOrder;
import com.berlin.aetherflow.wms.domain.entity.OutboundOrderItem;
import com.berlin.aetherflow.wms.domain.entity.PickingTask;
import com.berlin.aetherflow.wms.domain.vo.OutboundAllocationPreviewVo;
import com.berlin.aetherflow.wms.mapper.InventoryMapper;
import com.berlin.aetherflow.wms.mapper.LocationMapper;
import com.berlin.aetherflow.wms.mapper.MaterialMapper;
import com.berlin.aetherflow.wms.mapper.OutboundAllocationMapper;
import com.berlin.aetherflow.wms.mapper.OutboundOrderMapper;
import com.berlin.aetherflow.wms.mapper.PickingTaskMapper;
import com.berlin.aetherflow.wms.service.InventoryService;
import com.berlin.aetherflow.wms.service.OutboundAllocationService;
import com.berlin.aetherflow.wms.service.OutboundOrderItemService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 出库库存分配 Service 实现。
 */
@Service
@AllArgsConstructor
public class OutboundAllocationServiceImpl implements OutboundAllocationService {

    private final OutboundOrderMapper outboundOrderMapper;
    private final OutboundOrderItemService outboundOrderItemService;
    private final InventoryMapper inventoryMapper;
    private final OutboundAllocationMapper outboundAllocationMapper;
    private final InventoryService inventoryService;
    private final LocationMapper locationMapper;
    private final MaterialMapper materialMapper;
    private final PickingTaskMapper pickingTaskMapper;

    @Override
    public OutboundAllocationPreviewVo previewAllocation(Long orderId) {
        OutboundOrder order = outboundOrderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("出库单不存在");
        }

        List<OutboundOrderItem> orderItems = outboundOrderItemService.lambdaQuery()
                .eq(OutboundOrderItem::getOrderId, orderId)
                .orderByAsc(OutboundOrderItem::getLineNo)
                .list();
        if (orderItems == null || orderItems.isEmpty()) {
            throw new RuntimeException("出库单明细不能为空");
        }

        Map<Long, Location> locationMap = loadLocationMap(orderItems);
        Map<Long, Material> materialMap = loadMaterialMap(orderItems);
        List<Inventory> inventories = loadInventories(order, orderItems);
        Map<Long, BigDecimal> remainingAvailableMap = inventories.stream()
                .collect(Collectors.toMap(Inventory::getId, this::allocatableAvailable, (left, right) -> left));

        List<OutboundAllocationPreviewVo.Item> itemVos = new ArrayList<>(orderItems.size());
        BigDecimal totalRequiredQty = BigDecimal.ZERO;
        BigDecimal totalAllocatableQty = BigDecimal.ZERO;
        BigDecimal totalShortageQty = BigDecimal.ZERO;

        List<OutboundOrderItem> sortedItems = orderItems.stream()
                .sorted(Comparator.comparing(OutboundOrderItem::getLineNo, Comparator.nullsLast(Integer::compareTo)))
                .toList();
        for (OutboundOrderItem item : sortedItems) {
            OutboundAllocationPreviewVo.Item itemVo = buildItemPreview(
                    order,
                    item,
                    locationMap.get(item.getLocationId()),
                    materialMap.get(item.getMaterialId()),
                    inventories,
                    remainingAvailableMap
            );
            itemVos.add(itemVo);
            totalRequiredQty = totalRequiredQty.add(itemVo.getRequiredQty());
            totalAllocatableQty = totalAllocatableQty.add(itemVo.getAllocatableQty());
            totalShortageQty = totalShortageQty.add(itemVo.getShortageQty());
        }

        OutboundAllocationPreviewVo preview = new OutboundAllocationPreviewVo();
        preview.setOrderId(order.getId());
        preview.setOrderNo(order.getOrderNo());
        preview.setStatus(order.getStatus());
        preview.setWarehouseId(order.getWarehouseId());
        preview.setRequiredQty(totalRequiredQty);
        preview.setAllocatableQty(totalAllocatableQty);
        preview.setShortageQty(totalShortageQty);
        preview.setSufficient(totalShortageQty.compareTo(BigDecimal.ZERO) == 0);
        preview.setItems(itemVos);
        return preview;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean allocate(Long orderId) {
        OutboundOrder order = loadOrder(orderId);
        if (!OrderStatusConst.DRAFT.equals(order.getStatus())) {
            throw new RuntimeException("仅草稿出库单允许分配库存");
        }
        if (hasActiveAllocation(orderId)) {
            return true;
        }

        OutboundAllocationPreviewVo preview = previewAllocation(orderId);
        if (!Boolean.TRUE.equals(preview.getSufficient())) {
            throw new RuntimeException("库存不足，无法分配");
        }

        List<OutboundOrderItem> orderItems = loadOrderItems(orderId);
        List<Inventory> inventories = loadInventories(order, orderItems);
        Map<Long, BigDecimal> remainingAvailableMap = inventories.stream()
                .collect(Collectors.toMap(Inventory::getId, this::allocatableAvailable, (left, right) -> left));
        LocalDateTime now = LocalDateTime.now();
        List<OutboundAllocation> allocations = new ArrayList<>(orderItems.size());
        for (OutboundOrderItem item : orderItems) {
            BigDecimal remainingQty = resolveRequiredQty(item);
            List<Inventory> candidates = findCandidateInventories(order, item, inventories);
            if (candidates.isEmpty()) {
                throw new RuntimeException(withLinePrefix(item, "库位不存在当前物料库存"));
            }

            for (Inventory inventory : candidates) {
                if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }
                BigDecimal availableQty = normalizeQuantity(remainingAvailableMap.get(inventory.getId()));
                BigDecimal lockQty = remainingQty.min(availableQty);
                if (lockQty.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                int locked = inventoryMapper.lockAvailableQuantity(inventory.getId(), lockQty, resolveOperator());
                if (locked != 1) {
                    throw new RuntimeException(withLinePrefix(item, "可用库存不足，无法分配"));
                }
                remainingQty = remainingQty.subtract(lockQty);
                remainingAvailableMap.put(inventory.getId(), availableQty.subtract(lockQty));

                OutboundAllocation allocation = new OutboundAllocation();
                allocation.setOrderId(order.getId());
                allocation.setOrderItemId(item.getId());
                allocation.setLineNo(item.getLineNo());
                allocation.setInventoryId(inventory.getId());
                allocation.setWarehouseId(order.getWarehouseId());
                allocation.setLocationId(item.getLocationId());
                allocation.setMaterialId(item.getMaterialId());
                allocation.setBatchNo(normalizeBatchNo(inventory.getBatchNo()));
                allocation.setProductionDate(inventory.getProductionDate());
                allocation.setExpiryDate(inventory.getExpiryDate());
                allocation.setAllocatedQty(lockQty);
                allocation.setStatus(OutboundAllocationStatusConst.ACTIVE);
                allocation.setAllocateTime(now);
                allocation.setRemark(StringUtils.defaultIfBlank(item.getRemark(), order.getRemark()));
                allocations.add(allocation);
            }
            if (remainingQty.compareTo(BigDecimal.ZERO) > 0) {
                throw new RuntimeException(withLinePrefix(item, "可用库存不足，无法分配"));
            }
        }

        for (OutboundAllocation allocation : allocations) {
            outboundAllocationMapper.insert(allocation);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean releaseAllocation(Long orderId) {
        ensureNoUnclosedPickingTask(orderId);
        List<OutboundAllocation> allocations = listActiveAllocations(orderId);
        if (allocations.isEmpty()) {
            return true;
        }

        LocalDateTime now = LocalDateTime.now();
        for (OutboundAllocation allocation : allocations) {
            int released = inventoryMapper.releaseLockedQuantity(
                    allocation.getInventoryId(),
                    allocation.getAllocatedQty(),
                    resolveOperator()
            );
            if (released != 1) {
                throw new RuntimeException(withLinePrefix(allocation, "锁定库存不足，无法释放"));
            }
            allocation.setStatus(OutboundAllocationStatusConst.RELEASED);
            allocation.setReleaseTime(now);
            outboundAllocationMapper.updateById(allocation);
        }
        return true;
    }

    private void ensureNoUnclosedPickingTask(Long orderId) {
        Long count = pickingTaskMapper.selectCount(Wrappers.<PickingTask>lambdaQuery()
                .eq(PickingTask::getOutboundOrderId, orderId)
                .ne(PickingTask::getStatus, PickingTaskStatusConst.CANCELLED));
        if (count != null && count > 0) {
            throw new RuntimeException("出库单已生成拣货任务，请先取消拣货任务后再释放库存分配");
        }
    }

    @Override
    public boolean hasActiveAllocation(Long orderId) {
        Long count = outboundAllocationMapper.selectCount(Wrappers.<OutboundAllocation>lambdaQuery()
                .eq(OutboundAllocation::getOrderId, orderId)
                .eq(OutboundAllocation::getStatus, OutboundAllocationStatusConst.ACTIVE));
        return count != null && count > 0;
    }

    @Override
    public void ensureNoActiveAllocation(Long orderId) {
        if (hasActiveAllocation(orderId)) {
            throw new RuntimeException("出库单已分配库存，请先释放分配后再操作");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consumeActiveAllocation(OutboundOrder order, List<OutboundOrderItem> orderItems,
                                        LocalDateTime operateTime) {
        List<OutboundAllocation> allocations = listActiveAllocations(order.getId());
        if (allocations.isEmpty()) {
            throw new RuntimeException("出库单不存在可消费的库存分配");
        }

        List<StockChangeBo> changes = allocations.stream()
                .map(allocation -> toStockChange(order, allocation, operateTime))
                .toList();
        inventoryService.consumeLockedStockChanges(changes);

        Map<Long, BigDecimal> shippedQtyMap = allocations.stream()
                .collect(Collectors.groupingBy(
                        OutboundAllocation::getOrderItemId,
                        Collectors.reducing(BigDecimal.ZERO, OutboundAllocation::getAllocatedQty, BigDecimal::add)
                ));
        for (OutboundOrderItem item : orderItems) {
            BigDecimal shippedQty = shippedQtyMap.get(item.getId());
            if (shippedQty != null) {
                item.setShippedQty(shippedQty);
            }
        }

        LocalDateTime now = operateTime != null ? operateTime : LocalDateTime.now();
        for (OutboundAllocation allocation : allocations) {
            allocation.setStatus(OutboundAllocationStatusConst.CONSUMED);
            allocation.setConsumeTime(now);
            outboundAllocationMapper.updateById(allocation);
        }
    }

    private OutboundOrder loadOrder(Long orderId) {
        OutboundOrder order = outboundOrderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("出库单不存在");
        }
        return order;
    }

    private List<OutboundOrderItem> loadOrderItems(Long orderId) {
        List<OutboundOrderItem> orderItems = outboundOrderItemService.lambdaQuery()
                .eq(OutboundOrderItem::getOrderId, orderId)
                .orderByAsc(OutboundOrderItem::getLineNo)
                .list();
        if (orderItems == null || orderItems.isEmpty()) {
            throw new RuntimeException("出库单明细不能为空");
        }
        return orderItems;
    }

    private List<OutboundAllocation> listActiveAllocations(Long orderId) {
        return outboundAllocationMapper.selectList(Wrappers.<OutboundAllocation>lambdaQuery()
                .eq(OutboundAllocation::getOrderId, orderId)
                .eq(OutboundAllocation::getStatus, OutboundAllocationStatusConst.ACTIVE)
                .orderByAsc(OutboundAllocation::getLineNo, OutboundAllocation::getId));
    }

    private StockChangeBo toStockChange(OutboundOrder order, OutboundAllocation allocation, LocalDateTime operateTime) {
        StockChangeBo change = new StockChangeBo();
        change.setBizType(StockBizTypeConst.OUTBOUND_ORDER);
        change.setBizId(order.getId());
        change.setWarehouseId(allocation.getWarehouseId());
        change.setLocationId(allocation.getLocationId());
        change.setMaterialId(allocation.getMaterialId());
        change.setBatchNo(normalizeBatchNo(allocation.getBatchNo()));
        change.setProductionDate(allocation.getProductionDate());
        change.setExpiryDate(allocation.getExpiryDate());
        change.setLineNo(allocation.getLineNo());
        change.setChangeQty(allocation.getAllocatedQty().negate());
        change.setOperateTime(operateTime);
        change.setRemark(StringUtils.defaultString(allocation.getRemark()));
        return change;
    }

    private String withLinePrefix(OutboundOrderItem item, String message) {
        if (item.getLineNo() == null) {
            return message;
        }
        return "行号 " + item.getLineNo() + "：" + message;
    }

    private String withLinePrefix(OutboundAllocation allocation, String message) {
        if (allocation.getLineNo() == null) {
            return message;
        }
        return "行号 " + allocation.getLineNo() + "：" + message;
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

    private OutboundAllocationPreviewVo.Item buildItemPreview(OutboundOrder order,
                                                              OutboundOrderItem item,
                                                              Location location,
                                                              Material material,
                                                              List<Inventory> inventories,
                                                              Map<Long, BigDecimal> remainingAvailableMap) {
        OutboundAllocationPreviewVo.Item itemVo = new OutboundAllocationPreviewVo.Item();
        itemVo.setOrderItemId(item.getId());
        itemVo.setLineNo(item.getLineNo());
        itemVo.setMaterialId(item.getMaterialId());
        itemVo.setBatchNo(normalizeBatchNo(item.getBatchNo()));
        itemVo.setProductionDate(item.getProductionDate());
        itemVo.setExpiryDate(item.getExpiryDate());
        itemVo.setLocationId(item.getLocationId());
        itemVo.setRequiredQty(resolveRequiredQty(item));

        if (material != null) {
            itemVo.setMaterialCode(material.getMaterialCode());
            itemVo.setMaterialName(material.getMaterialName());
        }
        if (location != null) {
            itemVo.setLocationCode(location.getLocationCode());
            itemVo.setLocationName(location.getLocationName());
        }

        List<Inventory> candidates = findCandidateInventories(order, item, inventories);
        BigDecimal quantity = candidates.stream()
                .map(Inventory::getQuantity)
                .map(this::normalizeQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal lockedQuantity = candidates.stream()
                .map(Inventory::getLockedQuantity)
                .map(this::normalizeQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal frozenQuantity = candidates.stream()
                .map(Inventory::getFrozenQuantity)
                .map(this::normalizeQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal remainingAvailable = sumRemainingAvailable(candidates, remainingAvailableMap);
        itemVo.setQuantity(quantity);
        itemVo.setLockedQuantity(lockedQuantity);
        itemVo.setAvailableQuantity(quantity.subtract(lockedQuantity).subtract(frozenQuantity).max(BigDecimal.ZERO));

        BigDecimal allocatableQty = itemVo.getRequiredQty().min(remainingAvailable);
        BigDecimal shortageQty = itemVo.getRequiredQty().subtract(allocatableQty);
        List<OutboundAllocationPreviewVo.Segment> segments = buildPreviewSegments(location, candidates, remainingAvailableMap, allocatableQty);

        itemVo.setAllocatableQty(allocatableQty);
        itemVo.setShortageQty(shortageQty);
        itemVo.setAllocatable(shortageQty.compareTo(BigDecimal.ZERO) == 0);
        itemVo.setSegments(segments);
        itemVo.setMessage(resolveMessage(order, item, location, candidates, itemVo));
        return itemVo;
    }

    private Map<Long, Location> loadLocationMap(List<OutboundOrderItem> orderItems) {
        Set<Long> locationIds = orderItems.stream()
                .map(OutboundOrderItem::getLocationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (locationIds.isEmpty()) {
            return Map.of();
        }
        return locationMapper.selectByIds(locationIds).stream()
                .collect(Collectors.toMap(Location::getId, location -> location, (left, right) -> left));
    }

    private Map<Long, Material> loadMaterialMap(List<OutboundOrderItem> orderItems) {
        Set<Long> materialIds = orderItems.stream()
                .map(OutboundOrderItem::getMaterialId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (materialIds.isEmpty()) {
            return Map.of();
        }
        return materialMapper.selectByIds(materialIds).stream()
                .collect(Collectors.toMap(Material::getId, material -> material, (left, right) -> left));
    }

    private List<Inventory> loadInventories(OutboundOrder order, List<OutboundOrderItem> orderItems) {
        Set<Long> locationIds = orderItems.stream()
                .map(OutboundOrderItem::getLocationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> materialIds = orderItems.stream()
                .map(OutboundOrderItem::getMaterialId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (locationIds.isEmpty() || materialIds.isEmpty()) {
            return List.of();
        }

        return inventoryMapper.selectList(Wrappers.<Inventory>lambdaQuery()
                .eq(Inventory::getWarehouseId, order.getWarehouseId())
                .in(Inventory::getLocationId, locationIds)
                .in(Inventory::getMaterialId, materialIds)
                .gt(Inventory::getQuantity, BigDecimal.ZERO)
                .orderByAsc(Inventory::getExpiryDate, Inventory::getInboundTime, Inventory::getId));
    }

    private String resolveMessage(OutboundOrder order,
                                  OutboundOrderItem item,
                                  Location location,
                                  List<Inventory> candidates,
                                  OutboundAllocationPreviewVo.Item itemVo) {
        if (item.getLocationId() == null) {
            return "来源库位不能为空";
        }
        if (location == null) {
            return "来源库位不存在";
        }
        if (!Objects.equals(location.getWarehouseId(), order.getWarehouseId())) {
            return "来源库位不属于当前仓库";
        }
        if (candidates == null || candidates.isEmpty()) {
            return "库位不存在当前物料库存";
        }
        if (itemVo.getShortageQty().compareTo(BigDecimal.ZERO) > 0) {
            return "可用库存不足";
        }
        return "可分配";
    }

    private List<Inventory> findCandidateInventories(OutboundOrder order, OutboundOrderItem item, List<Inventory> inventories) {
        String batchNo = normalizeBatchNo(item.getBatchNo());
        return inventories.stream()
                .filter(inventory -> Objects.equals(inventory.getWarehouseId(), order.getWarehouseId()))
                .filter(inventory -> Objects.equals(inventory.getLocationId(), item.getLocationId()))
                .filter(inventory -> Objects.equals(inventory.getMaterialId(), item.getMaterialId()))
                .filter(inventory -> StringUtils.isBlank(batchNo)
                        || Objects.equals(normalizeBatchNo(inventory.getBatchNo()), batchNo))
                .sorted(Comparator
                        .comparing(Inventory::getExpiryDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Inventory::getInboundTime, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Inventory::getId, Comparator.nullsLast(Long::compareTo)))
                .toList();
    }

    private BigDecimal sumRemainingAvailable(List<Inventory> candidates, Map<Long, BigDecimal> remainingAvailableMap) {
        return candidates.stream()
                .map(Inventory::getId)
                .map(remainingAvailableMap::get)
                .map(this::normalizeQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<OutboundAllocationPreviewVo.Segment> buildPreviewSegments(Location location,
                                                                           List<Inventory> candidates,
                                                                           Map<Long, BigDecimal> remainingAvailableMap,
                                                                           BigDecimal requiredQty) {
        List<OutboundAllocationPreviewVo.Segment> segments = new ArrayList<>();
        BigDecimal remainingQty = normalizeQuantity(requiredQty);
        for (Inventory inventory : candidates) {
            if (remainingQty.compareTo(BigDecimal.ZERO) <= 0) {
                return segments;
            }
            BigDecimal available = normalizeQuantity(remainingAvailableMap.get(inventory.getId()));
            BigDecimal consumedQty = remainingQty.min(available);
            if (consumedQty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            OutboundAllocationPreviewVo.Segment segment = new OutboundAllocationPreviewVo.Segment();
            segment.setInventoryId(inventory.getId());
            segment.setLocationId(inventory.getLocationId());
            if (location != null) {
                segment.setLocationCode(location.getLocationCode());
                segment.setLocationName(location.getLocationName());
            }
            segment.setBatchNo(normalizeBatchNo(inventory.getBatchNo()));
            segment.setProductionDate(inventory.getProductionDate());
            segment.setExpiryDate(inventory.getExpiryDate());
            segment.setAvailableQuantity(available);
            segment.setAllocatableQty(consumedQty);
            segments.add(segment);

            remainingAvailableMap.put(inventory.getId(), available.subtract(consumedQty));
            remainingQty = remainingQty.subtract(consumedQty);
        }
        return segments;
    }

    private BigDecimal resolveRequiredQty(OutboundOrderItem item) {
        BigDecimal requiredQty = item.getShippedQty();
        if (requiredQty == null || requiredQty.compareTo(BigDecimal.ZERO) <= 0) {
            requiredQty = item.getPlannedQty();
        }
        return normalizeQuantity(requiredQty);
    }

    private BigDecimal availableQuantity(Inventory inventory) {
        return normalizeQuantity(inventory.getQuantity())
                .subtract(normalizeQuantity(inventory.getLockedQuantity()))
                .subtract(normalizeQuantity(inventory.getFrozenQuantity()));
    }

    private BigDecimal allocatableAvailable(Inventory inventory) {
        return availableQuantity(inventory).max(BigDecimal.ZERO);
    }

    private BigDecimal normalizeQuantity(BigDecimal quantity) {
        return quantity == null ? BigDecimal.ZERO : quantity;
    }

    private String normalizeBatchNo(String batchNo) {
        return StringUtils.defaultString(StringUtils.trimToNull(batchNo));
    }
}
