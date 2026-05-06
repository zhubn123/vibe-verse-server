package com.berlin.aetherflow.wms.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.berlin.aetherflow.wms.constant.OrderStatusConst;
import com.berlin.aetherflow.wms.domain.entity.Inventory;
import com.berlin.aetherflow.wms.domain.entity.InventoryAdjustment;
import com.berlin.aetherflow.wms.domain.entity.InboundOrder;
import com.berlin.aetherflow.wms.domain.entity.Location;
import com.berlin.aetherflow.wms.domain.entity.Material;
import com.berlin.aetherflow.wms.domain.entity.OutboundOrder;
import com.berlin.aetherflow.wms.domain.entity.Warehouse;
import com.berlin.aetherflow.wms.domain.vo.WmsWorkbenchVo;
import com.berlin.aetherflow.wms.mapper.InventoryAdjustmentMapper;
import com.berlin.aetherflow.wms.mapper.InventoryMapper;
import com.berlin.aetherflow.wms.mapper.InboundOrderMapper;
import com.berlin.aetherflow.wms.mapper.LocationMapper;
import com.berlin.aetherflow.wms.mapper.MaterialMapper;
import com.berlin.aetherflow.wms.mapper.OutboundOrderMapper;
import com.berlin.aetherflow.wms.mapper.WarehouseMapper;
import com.berlin.aetherflow.wms.service.WmsWorkbenchService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * WMS workbench service implementation.
 */
@Service
@AllArgsConstructor
public class WmsWorkbenchServiceImpl implements WmsWorkbenchService {

    private static final BigDecimal LOW_STOCK_THRESHOLD = new BigDecimal("10");
    private static final BigDecimal HIGH_STOCK_THRESHOLD = new BigDecimal("100");
    private static final DateTimeFormatter DATE_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");
    private static final DateTimeFormatter ACTIVITY_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final WarehouseMapper warehouseMapper;
    private final InventoryMapper inventoryMapper;
    private final MaterialMapper materialMapper;
    private final LocationMapper locationMapper;
    private final InboundOrderMapper inboundOrderMapper;
    private final OutboundOrderMapper outboundOrderMapper;
    private final InventoryAdjustmentMapper inventoryAdjustmentMapper;

    @Override
    public WmsWorkbenchVo getOverview(Long warehouseId, Integer days) {
        int rangeDays = normalizeDays(days);
        List<Inventory> inventories = inventoryMapper.selectList(Wrappers.<Inventory>lambdaQuery()
                .eq(warehouseId != null, Inventory::getWarehouseId, warehouseId));

        WmsWorkbenchVo overview = new WmsWorkbenchVo();
        overview.setWarehouses(buildWarehouseOptions());
        overview.setSummary(buildSummary(warehouseId, inventories));
        overview.setWarnings(buildWarnings(inventories));
        overview.setTrends(buildTrendPoints(warehouseId, rangeDays));
        overview.setActivities(buildActivities(warehouseId));
        return overview;
    }

    private int normalizeDays(Integer days) {
        if (days == null) {
            return 7;
        }
        return Math.max(1, Math.min(days, 14));
    }

    private List<WmsWorkbenchVo.WarehouseOption> buildWarehouseOptions() {
        return warehouseMapper.selectList(Wrappers.<Warehouse>lambdaQuery()
                        .eq(Warehouse::getStatus, 0)
                        .orderByAsc(Warehouse::getWarehouseCode))
                .stream()
                .map(warehouse -> {
                    WmsWorkbenchVo.WarehouseOption option = new WmsWorkbenchVo.WarehouseOption();
                    option.setId(warehouse.getId());
                    option.setWarehouseCode(warehouse.getWarehouseCode());
                    option.setWarehouseName(warehouse.getWarehouseName());
                    return option;
                })
                .toList();
    }

    private WmsWorkbenchVo.Summary buildSummary(Long warehouseId, List<Inventory> inventories) {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();
        LocalDateTime yesterdayStart = today.minusDays(1).atStartOfDay();

        WmsWorkbenchVo.Summary summary = new WmsWorkbenchVo.Summary();
        summary.setWarningCount(inventories.stream().filter(this::isWarningInventory).count());
        summary.setExceptionCount(countExceptionOrders(warehouseId));
        summary.setTodayInboundCount(countInboundOrders(warehouseId, todayStart, tomorrowStart));
        summary.setYesterdayInboundCount(countInboundOrders(warehouseId, yesterdayStart, todayStart));
        summary.setTodayOutboundCount(countOutboundOrders(warehouseId, todayStart, tomorrowStart));
        summary.setYesterdayOutboundCount(countOutboundOrders(warehouseId, yesterdayStart, todayStart));
        summary.setStockQuantity(inventories.stream()
                .map(Inventory::getQuantity)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        summary.setStockedMaterialCount(inventories.stream()
                .filter(inventory -> safeQuantity(inventory).compareTo(BigDecimal.ZERO) > 0)
                .map(Inventory::getMaterialId)
                .filter(Objects::nonNull)
                .distinct()
                .count());
        return summary;
    }

    private Long countExceptionOrders(Long warehouseId) {
        return nullToZero(inboundOrderMapper.selectCount(Wrappers.<InboundOrder>lambdaQuery()
                .eq(warehouseId != null, InboundOrder::getWarehouseId, warehouseId)
                .eq(InboundOrder::getStatus, OrderStatusConst.DRAFT)))
                + nullToZero(outboundOrderMapper.selectCount(Wrappers.<OutboundOrder>lambdaQuery()
                .eq(warehouseId != null, OutboundOrder::getWarehouseId, warehouseId)
                .eq(OutboundOrder::getStatus, OrderStatusConst.DRAFT)))
                + nullToZero(inventoryAdjustmentMapper.selectCount(Wrappers.<InventoryAdjustment>lambdaQuery()
                .eq(warehouseId != null, InventoryAdjustment::getWarehouseId, warehouseId)
                .eq(InventoryAdjustment::getStatus, OrderStatusConst.DRAFT)));
    }

    private Long countInboundOrders(Long warehouseId, LocalDateTime startTime, LocalDateTime endTime) {
        return nullToZero(inboundOrderMapper.selectCount(Wrappers.<InboundOrder>lambdaQuery()
                .eq(warehouseId != null, InboundOrder::getWarehouseId, warehouseId)
                .ge(InboundOrder::getCreateTime, startTime)
                .lt(InboundOrder::getCreateTime, endTime)));
    }

    private Long countOutboundOrders(Long warehouseId, LocalDateTime startTime, LocalDateTime endTime) {
        return nullToZero(outboundOrderMapper.selectCount(Wrappers.<OutboundOrder>lambdaQuery()
                .eq(warehouseId != null, OutboundOrder::getWarehouseId, warehouseId)
                .ge(OutboundOrder::getCreateTime, startTime)
                .lt(OutboundOrder::getCreateTime, endTime)));
    }

    private List<WmsWorkbenchVo.WarningRow> buildWarnings(List<Inventory> inventories) {
        List<Inventory> warningInventories = inventories.stream()
                .filter(this::isWarningInventory)
                .sorted(Comparator.comparingInt(this::warningPriority)
                        .thenComparing(this::safeQuantity))
                .limit(5)
                .toList();
        if (warningInventories.isEmpty()) {
            return List.of();
        }

        Set<Long> materialIds = warningInventories.stream()
                .map(Inventory::getMaterialId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> warehouseIds = warningInventories.stream()
                .map(Inventory::getWarehouseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> locationIds = warningInventories.stream()
                .map(Inventory::getLocationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Material> materialMap = materialIds.isEmpty()
                ? Map.of()
                : materialMapper.selectByIds(materialIds).stream()
                .collect(Collectors.toMap(Material::getId, Function.identity(), (left, right) -> left));
        Map<Long, Warehouse> warehouseMap = warehouseIds.isEmpty()
                ? Map.of()
                : warehouseMapper.selectByIds(warehouseIds).stream()
                .collect(Collectors.toMap(Warehouse::getId, Function.identity(), (left, right) -> left));
        Map<Long, Location> locationMap = locationIds.isEmpty()
                ? Map.of()
                : locationMapper.selectByIds(locationIds).stream()
                .collect(Collectors.toMap(Location::getId, Function.identity(), (left, right) -> left));

        return warningInventories.stream()
                .map(inventory -> toWarningRow(inventory, materialMap, warehouseMap, locationMap))
                .toList();
    }

    private WmsWorkbenchVo.WarningRow toWarningRow(Inventory inventory,
                                                   Map<Long, Material> materialMap,
                                                   Map<Long, Warehouse> warehouseMap,
                                                   Map<Long, Location> locationMap) {
        Material material = materialMap.get(inventory.getMaterialId());
        Warehouse warehouse = warehouseMap.get(inventory.getWarehouseId());
        Location location = locationMap.get(inventory.getLocationId());
        BigDecimal quantity = safeQuantity(inventory);

        WmsWorkbenchVo.WarningRow row = new WmsWorkbenchVo.WarningRow();
        row.setInventoryId(inventory.getId());
        row.setWarehouseId(inventory.getWarehouseId());
        row.setWarehouseName(warehouse == null ? null : warehouse.getWarehouseName());
        row.setLocationCode(location == null ? null : location.getLocationCode());
        row.setMaterialCode(material == null ? null : material.getMaterialCode());
        row.setMaterialName(material == null ? null : material.getMaterialName());
        row.setSpecification(material == null ? null : material.getSpecification());
        row.setStock(quantity);
        if (quantity.compareTo(LOW_STOCK_THRESHOLD) <= 0) {
            row.setLevel("danger");
            row.setLevelLabel("库存不足");
        } else {
            row.setLevel("warning");
            row.setLevelLabel("库存积压");
        }
        return row;
    }

    private List<WmsWorkbenchVo.TrendPoint> buildTrendPoints(Long warehouseId, int days) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1L);
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = today.plusDays(1).atStartOfDay();

        Map<LocalDate, Long> inboundCountMap = inboundOrderMapper.selectList(Wrappers.<InboundOrder>lambdaQuery()
                        .eq(warehouseId != null, InboundOrder::getWarehouseId, warehouseId)
                        .ge(InboundOrder::getCreateTime, startTime)
                        .lt(InboundOrder::getCreateTime, endTime))
                .stream()
                .map(InboundOrder::getCreateTime)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(LocalDateTime::toLocalDate, Collectors.counting()));
        Map<LocalDate, Long> outboundCountMap = outboundOrderMapper.selectList(Wrappers.<OutboundOrder>lambdaQuery()
                        .eq(warehouseId != null, OutboundOrder::getWarehouseId, warehouseId)
                        .ge(OutboundOrder::getCreateTime, startTime)
                        .lt(OutboundOrder::getCreateTime, endTime))
                .stream()
                .map(OutboundOrder::getCreateTime)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(LocalDateTime::toLocalDate, Collectors.counting()));

        List<WmsWorkbenchVo.TrendPoint> points = new ArrayList<>(days);
        for (int i = 0; i < days; i++) {
            LocalDate date = startDate.plusDays(i);
            WmsWorkbenchVo.TrendPoint point = new WmsWorkbenchVo.TrendPoint();
            point.setDate(date.toString());
            point.setLabel(DATE_LABEL_FORMATTER.format(date));
            point.setInboundCount(inboundCountMap.getOrDefault(date, 0L));
            point.setOutboundCount(outboundCountMap.getOrDefault(date, 0L));
            points.add(point);
        }
        return points;
    }

    private List<WmsWorkbenchVo.Activity> buildActivities(Long warehouseId) {
        List<ActivityCandidate> candidates = new ArrayList<>();
        appendInboundActivities(warehouseId, candidates);
        appendOutboundActivities(warehouseId, candidates);
        appendAdjustmentActivities(warehouseId, candidates);

        return candidates.stream()
                .sorted((left, right) -> nullSafeTime(right.time()).compareTo(nullSafeTime(left.time())))
                .limit(5)
                .map(ActivityCandidate::activity)
                .toList();
    }

    private void appendInboundActivities(Long warehouseId, List<ActivityCandidate> candidates) {
        Page<InboundOrder> page = inboundOrderMapper.selectPage(new Page<>(1, 5), Wrappers.<InboundOrder>lambdaQuery()
                .eq(warehouseId != null, InboundOrder::getWarehouseId, warehouseId)
                .orderByDesc(InboundOrder::getUpdateTime));
        for (InboundOrder order : page.getRecords()) {
            LocalDateTime time = OrderStatusConst.CONFIRMED.equals(order.getStatus())
                    ? firstNonNull(order.getInboundTime(), order.getUpdateTime(), order.getCreateTime())
                    : firstNonNull(order.getUpdateTime(), order.getCreateTime());
            String title = OrderStatusConst.CONFIRMED.equals(order.getStatus())
                    ? "入库单 " + order.getOrderNo() + " 已完成入库"
                    : "入库单 " + order.getOrderNo() + " 待确认";
            candidates.add(new ActivityCandidate(time, buildActivity(time, title, resolveUser(order.getUpdateBy(), order.getCreateBy()), "inbound")));
        }
    }

    private void appendOutboundActivities(Long warehouseId, List<ActivityCandidate> candidates) {
        Page<OutboundOrder> page = outboundOrderMapper.selectPage(new Page<>(1, 5), Wrappers.<OutboundOrder>lambdaQuery()
                .eq(warehouseId != null, OutboundOrder::getWarehouseId, warehouseId)
                .orderByDesc(OutboundOrder::getUpdateTime));
        for (OutboundOrder order : page.getRecords()) {
            LocalDateTime time = OrderStatusConst.CONFIRMED.equals(order.getStatus())
                    ? firstNonNull(order.getOutboundTime(), order.getUpdateTime(), order.getCreateTime())
                    : firstNonNull(order.getUpdateTime(), order.getCreateTime());
            String title = OrderStatusConst.CONFIRMED.equals(order.getStatus())
                    ? "出库单 " + order.getOrderNo() + " 已完成出库"
                    : "出库单 " + order.getOrderNo() + " 待确认";
            candidates.add(new ActivityCandidate(time, buildActivity(time, title, resolveUser(order.getUpdateBy(), order.getCreateBy()), "outbound")));
        }
    }

    private void appendAdjustmentActivities(Long warehouseId, List<ActivityCandidate> candidates) {
        Page<InventoryAdjustment> page = inventoryAdjustmentMapper.selectPage(new Page<>(1, 5), Wrappers.<InventoryAdjustment>lambdaQuery()
                .eq(warehouseId != null, InventoryAdjustment::getWarehouseId, warehouseId)
                .orderByDesc(InventoryAdjustment::getUpdateTime));
        for (InventoryAdjustment order : page.getRecords()) {
            LocalDateTime time = OrderStatusConst.CONFIRMED.equals(order.getStatus())
                    ? firstNonNull(order.getAdjustTime(), order.getUpdateTime(), order.getCreateTime())
                    : firstNonNull(order.getUpdateTime(), order.getCreateTime());
            String title = OrderStatusConst.CONFIRMED.equals(order.getStatus())
                    ? "库存调整单 " + order.getOrderNo() + " 已确认"
                    : "库存调整单 " + order.getOrderNo() + " 待确认";
            candidates.add(new ActivityCandidate(time, buildActivity(time, title, resolveUser(order.getUpdateBy(), order.getCreateBy()), "adjustment")));
        }
    }

    private WmsWorkbenchVo.Activity buildActivity(LocalDateTime time, String title, String user, String type) {
        WmsWorkbenchVo.Activity activity = new WmsWorkbenchVo.Activity();
        activity.setTime(time == null ? "-" : ACTIVITY_TIME_FORMATTER.format(time));
        activity.setTitle(title);
        activity.setUser(user);
        activity.setType(type);
        return activity;
    }

    private boolean isWarningInventory(Inventory inventory) {
        BigDecimal quantity = safeQuantity(inventory);
        return quantity.compareTo(LOW_STOCK_THRESHOLD) <= 0
                || quantity.compareTo(HIGH_STOCK_THRESHOLD) >= 0;
    }

    private int warningPriority(Inventory inventory) {
        return safeQuantity(inventory).compareTo(LOW_STOCK_THRESHOLD) <= 0 ? 0 : 1;
    }

    private BigDecimal safeQuantity(Inventory inventory) {
        if (inventory == null || inventory.getQuantity() == null) {
            return BigDecimal.ZERO;
        }
        return inventory.getQuantity();
    }

    private Long nullToZero(Long value) {
        return value == null ? 0L : value;
    }

    private LocalDateTime firstNonNull(LocalDateTime first, LocalDateTime second) {
        return first == null ? second : first;
    }

    private LocalDateTime firstNonNull(LocalDateTime first, LocalDateTime second, LocalDateTime third) {
        return firstNonNull(firstNonNull(first, second), third);
    }

    private LocalDateTime nullSafeTime(LocalDateTime time) {
        return time == null ? LocalDateTime.MIN : time;
    }

    private String resolveUser(String updateBy, String createBy) {
        return StringUtils.defaultIfBlank(updateBy, StringUtils.defaultIfBlank(createBy, "系统"));
    }

    private record ActivityCandidate(LocalDateTime time, WmsWorkbenchVo.Activity activity) {
    }
}
