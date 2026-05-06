package com.berlin.aetherflow.wms.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.common.utils.MapstructUtils;
import com.berlin.aetherflow.common.utils.OrderUtil;
import com.berlin.aetherflow.wms.domain.bo.StockChangeBo;
import com.berlin.aetherflow.wms.domain.bo.StockFreezeBo;
import com.berlin.aetherflow.wms.domain.entity.Area;
import com.berlin.aetherflow.wms.domain.entity.Inventory;
import com.berlin.aetherflow.wms.domain.entity.Location;
import com.berlin.aetherflow.wms.domain.entity.Material;
import com.berlin.aetherflow.wms.domain.entity.Warehouse;
import com.berlin.aetherflow.wms.constant.StockBizTypeConst;
import com.berlin.aetherflow.wms.constant.StockFreezeTypeConst;
import com.berlin.aetherflow.wms.domain.query.InventoryExpiryWarningQuery;
import com.berlin.aetherflow.wms.domain.query.InventoryQuery;
import com.berlin.aetherflow.wms.domain.vo.ExpiredInventoryFreezeResultVo;
import com.berlin.aetherflow.wms.domain.vo.InventoryExpiryWarningVo;
import com.berlin.aetherflow.wms.domain.vo.InventoryVo;
import com.berlin.aetherflow.wms.mapper.AreaMapper;
import com.berlin.aetherflow.wms.mapper.InventoryMapper;
import com.berlin.aetherflow.wms.mapper.LocationMapper;
import com.berlin.aetherflow.wms.mapper.MaterialMapper;
import com.berlin.aetherflow.wms.mapper.WarehouseMapper;
import com.berlin.aetherflow.wms.service.InventoryService;
import com.berlin.aetherflow.wms.service.StockTransactionService;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author berlin
* @description 针对表【Inventory(库存表)】的数据库操作Service实现
* @createDate 2026-04-15 16:17:27
*/
@Service
@AllArgsConstructor
public class InventoryServiceImpl extends ServiceImpl<InventoryMapper, Inventory>
    implements InventoryService {

    private final InventoryMapper inventoryMapper;
    private final WarehouseMapper warehouseMapper;
    private final LocationMapper locationMapper;
    private final MaterialMapper materialMapper;
    private final AreaMapper areaMapper;
    private final StockTransactionService stockTransactionService;
    private final TransactionTemplate transactionTemplate;

    @Override
    public PageResult<InventoryVo> queryList(InventoryQuery query) {
        LambdaQueryWrapper<Inventory> lqw = Wrappers.<Inventory>lambdaQuery()
                .eq(query.getLocationId() != null, Inventory::getLocationId, query.getLocationId())
                .eq(query.getWarehouseId() != null, Inventory::getWarehouseId, query.getWarehouseId())
                .eq(query.getMaterialId() != null, Inventory::getMaterialId, query.getMaterialId())
                .like(StringUtils.isNotBlank(query.getBatchNo()), Inventory::getBatchNo, query.getBatchNo())
                .ge(query.getMinQuantity() != null, Inventory::getQuantity, query.getMinQuantity())
                .le(query.getMaxQuantity() != null, Inventory::getQuantity, query.getMaxQuantity())
                .eq(query.getFrozenQuantity() != null, Inventory::getFrozenQuantity, query.getFrozenQuantity());

        if (query.getAreaId() != null) {
            LambdaQueryWrapper<Location> locationLqw = Wrappers.<Location>lambdaQuery()
                    .select(Location::getId)
                    .eq(Location::getAreaId, query.getAreaId())
                    .eq(query.getWarehouseId() != null, Location::getWarehouseId, query.getWarehouseId());
            List<Long> matchedLocationIds = locationMapper.selectList(locationLqw).stream()
                    .map(Location::getId)
                    .toList();
            if (matchedLocationIds.isEmpty()) {
                return PageResult.of(Long.valueOf(query.getPageNo()), Long.valueOf(query.getPageSize()), 0L, 0L, List.of());
            }
            lqw.in(Inventory::getLocationId, matchedLocationIds);
        }

        IPage<Inventory> page = new Page<>(query.getPageNo(), query.getPageSize());
        OrderUtil.addOrder(page, query.getSortBy(), query.getIsAsc());

        IPage<Inventory> result = inventoryMapper.selectPage(page, lqw);
        List<InventoryVo> records = result.getRecords().stream()
                .map(this::toInventoryVo)
                .filter(Objects::nonNull)
                .toList();
        fillDisplayFields(records);
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    @Override
    public PageResult<InventoryExpiryWarningVo> queryExpiryWarnings(InventoryExpiryWarningQuery query) {
        int warningDays = query.getDays() == null ? 30 : query.getDays();
        LocalDate today = LocalDate.now();
        LocalDate maxExpiryDate = today.plusDays(warningDays);

        LambdaQueryWrapper<Inventory> lqw = Wrappers.<Inventory>lambdaQuery()
                .eq(query.getLocationId() != null, Inventory::getLocationId, query.getLocationId())
                .eq(query.getWarehouseId() != null, Inventory::getWarehouseId, query.getWarehouseId())
                .eq(query.getMaterialId() != null, Inventory::getMaterialId, query.getMaterialId())
                .like(StringUtils.isNotBlank(query.getBatchNo()), Inventory::getBatchNo, query.getBatchNo())
                .isNotNull(Inventory::getExpiryDate)
                .le(Inventory::getExpiryDate, maxExpiryDate)
                .apply("coalesce(quantity, 0) - coalesce(locked_quantity, 0) - coalesce(frozen_quantity, 0) > 0");

        if (query.getAreaId() != null) {
            LambdaQueryWrapper<Location> locationLqw = Wrappers.<Location>lambdaQuery()
                    .select(Location::getId)
                    .eq(Location::getAreaId, query.getAreaId())
                    .eq(query.getWarehouseId() != null, Location::getWarehouseId, query.getWarehouseId());
            List<Long> matchedLocationIds = locationMapper.selectList(locationLqw).stream()
                    .map(Location::getId)
                    .toList();
            if (matchedLocationIds.isEmpty()) {
                return PageResult.of(Long.valueOf(query.getPageNo()), Long.valueOf(query.getPageSize()), 0L, 0L, List.of());
            }
            lqw.in(Inventory::getLocationId, matchedLocationIds);
        }

        if (StringUtils.isBlank(query.getSortBy()) || query.getIsAsc() == null) {
            lqw.orderByAsc(Inventory::getExpiryDate, Inventory::getInboundTime, Inventory::getId);
        }

        IPage<Inventory> page = new Page<>(query.getPageNo(), query.getPageSize());
        OrderUtil.addOrder(page, query.getSortBy(), query.getIsAsc());

        IPage<Inventory> result = inventoryMapper.selectPage(page, lqw);
        List<InventoryVo> inventoryVos = result.getRecords().stream()
                .map(this::toInventoryVo)
                .filter(Objects::nonNull)
                .toList();
        fillDisplayFields(inventoryVos);
        List<InventoryExpiryWarningVo> records = inventoryVos.stream()
                .map(vo -> toExpiryWarningVo(vo, today))
                .toList();
        return PageResult.of(result.getCurrent(), result.getSize(), result.getTotal(), result.getPages(), records);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void applyStockChanges(List<StockChangeBo> changes) {
        if (changes == null || changes.isEmpty()) {
            return;
        }

        for (StockChangeBo change : changes) {
            if (change == null) {
                continue;
            }
            validateStockChange(change);

            Location location = locationMapper.selectById(change.getLocationId());
            if (location == null) {
                throw new RuntimeException("库存变动库位不存在: " + change.getLocationId());
            }
            if (!Objects.equals(location.getWarehouseId(), change.getWarehouseId())) {
                throw new RuntimeException("库存变动仓库与库位不一致: " + change.getLocationId());
            }

            if (change.getChangeQty().compareTo(BigDecimal.ZERO) > 0) {
                applyInboundChange(change, location);
                continue;
            }
            applyOutboundChange(change, location);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void consumeLockedStockChanges(List<StockChangeBo> changes) {
        if (changes == null || changes.isEmpty()) {
            return;
        }

        for (StockChangeBo change : changes) {
            if (change == null) {
                continue;
            }
            validateStockChange(change);
            if (change.getChangeQty().compareTo(BigDecimal.ZERO) >= 0) {
                throw new RuntimeException(withLinePrefix(change, "锁定库存消费数量必须为负数"));
            }

            Location location = locationMapper.selectById(change.getLocationId());
            if (location == null) {
                throw new RuntimeException("库存变动库位不存在: " + change.getLocationId());
            }
            if (!Objects.equals(location.getWarehouseId(), change.getWarehouseId())) {
                throw new RuntimeException("库存变动仓库与库位不一致: " + change.getLocationId());
            }

            consumeLockedOutboundChange(change, location);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean freezeStock(Long id, StockFreezeBo bo) {
        return applyFrozenQuantityChange(id, bo, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean unfreezeStock(Long id, StockFreezeBo bo) {
        return applyFrozenQuantityChange(id, bo, false);
    }

    @Override
    public ExpiredInventoryFreezeResultVo freezeExpiredStocks() {
        List<Inventory> candidates = inventoryMapper.selectList(Wrappers.<Inventory>lambdaQuery()
                .isNotNull(Inventory::getExpiryDate)
                .lt(Inventory::getExpiryDate, LocalDate.now())
                .apply("coalesce(quantity, 0) - coalesce(locked_quantity, 0) - coalesce(frozen_quantity, 0) > 0")
                .orderByAsc(Inventory::getExpiryDate, Inventory::getInboundTime, Inventory::getId));

        ExpiredInventoryFreezeResultVo result = new ExpiredInventoryFreezeResultVo();
        result.setScannedCount(candidates.size());
        result.setFrozenCount(0);
        result.setFrozenQuantity(BigDecimal.ZERO);
        result.setSkippedCount(0);
        result.setFailedCount(0);
        result.setExecutedAt(LocalDateTime.now());

        for (Inventory candidate : candidates) {
            BigDecimal availableQuantity = availableQuantity(candidate);
            if (availableQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                result.setSkippedCount(result.getSkippedCount() + 1);
                continue;
            }

            StockFreezeBo bo = new StockFreezeBo();
            bo.setQuantity(availableQuantity);
            bo.setFreezeType(StockFreezeTypeConst.EXCEPTION);
            bo.setReason("效期过期自动冻结");

            try {
                Boolean frozen = transactionTemplate.execute(status -> applyFrozenQuantityChange(candidate.getId(), bo, true));
                if (Boolean.TRUE.equals(frozen)) {
                    result.setFrozenCount(result.getFrozenCount() + 1);
                    result.setFrozenQuantity(result.getFrozenQuantity().add(availableQuantity));
                } else {
                    result.setFailedCount(result.getFailedCount() + 1);
                }
            } catch (RuntimeException ex) {
                result.setFailedCount(result.getFailedCount() + 1);
            }
        }

        return result;
    }

    /**
     * 填充库存列表的展示字段（仓库/库位/物料编码和名称）。
     *
     * @param records 库存列表
     */
    private void fillDisplayFields(List<InventoryVo> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        Set<Long> warehouseIds = records.stream()
                .map(InventoryVo::getWarehouseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> locationIds = records.stream()
                .map(InventoryVo::getLocationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> materialIds = records.stream()
                .map(InventoryVo::getMaterialId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, Warehouse> warehouseMap = warehouseIds.isEmpty()
                ? Map.of()
                : warehouseMapper.selectByIds(warehouseIds).stream()
                .collect(Collectors.toMap(Warehouse::getId, warehouse -> warehouse, (left, right) -> left));
        Map<Long, Location> locationMap = locationIds.isEmpty()
                ? Map.of()
                : locationMapper.selectByIds(locationIds).stream()
                .collect(Collectors.toMap(Location::getId, location -> location, (left, right) -> left));
        Set<Long> areaIds = locationMap.values().stream()
                .map(Location::getAreaId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Area> areaMap = areaIds.isEmpty()
                ? Map.of()
                : areaMapper.selectByIds(areaIds).stream()
                .collect(Collectors.toMap(Area::getId, area -> area, (left, right) -> left));
        Map<Long, Material> materialMap = materialIds.isEmpty()
                ? Map.of()
                : materialMapper.selectByIds(materialIds).stream()
                .collect(Collectors.toMap(Material::getId, material -> material, (left, right) -> left));

        for (InventoryVo record : records) {
            Warehouse warehouse = warehouseMap.get(record.getWarehouseId());
            if (warehouse != null) {
                record.setWarehouseCode(warehouse.getWarehouseCode());
                record.setWarehouseName(warehouse.getWarehouseName());
            }

            Location location = locationMap.get(record.getLocationId());
            if (location != null) {
                record.setAreaId(location.getAreaId());
                record.setLocationCode(location.getLocationCode());
                record.setLocationName(location.getLocationName());
                Area area = areaMap.get(location.getAreaId());
                if (area != null) {
                    record.setAreaCode(area.getAreaCode());
                    record.setAreaName(area.getAreaName());
                }
            }

            Material material = materialMap.get(record.getMaterialId());
            if (material != null) {
                record.setMaterialCode(material.getMaterialCode());
                record.setMaterialName(material.getMaterialName());
            }
        }
    }

    private InventoryVo toInventoryVo(Inventory inventory) {
        InventoryVo vo = MapstructUtils.convert(inventory, InventoryVo.class);
        if (vo == null) {
            return null;
        }
        BigDecimal quantity = normalizeQuantity(vo.getQuantity());
        BigDecimal lockedQuantity = normalizeQuantity(vo.getLockedQuantity());
        BigDecimal frozenQuantity = normalizeQuantity(vo.getFrozenQuantity());
        vo.setQuantity(quantity);
        vo.setLockedQuantity(lockedQuantity);
        vo.setFrozenQuantity(frozenQuantity);
        vo.setAvailableQuantity(quantity.subtract(lockedQuantity).subtract(frozenQuantity));
        return vo;
    }

    private BigDecimal normalizeQuantity(BigDecimal quantity) {
        return quantity == null ? BigDecimal.ZERO : quantity;
    }

    private BigDecimal availableQuantity(Inventory inventory) {
        if (inventory == null) {
            return BigDecimal.ZERO;
        }
        return normalizeQuantity(inventory.getQuantity())
                .subtract(normalizeQuantity(inventory.getLockedQuantity()))
                .subtract(normalizeQuantity(inventory.getFrozenQuantity()));
    }

    private InventoryExpiryWarningVo toExpiryWarningVo(InventoryVo inventory, LocalDate today) {
        InventoryExpiryWarningVo vo = new InventoryExpiryWarningVo();
        vo.setInventoryId(inventory.getId());
        vo.setWarehouseId(inventory.getWarehouseId());
        vo.setWarehouseCode(inventory.getWarehouseCode());
        vo.setWarehouseName(inventory.getWarehouseName());
        vo.setAreaId(inventory.getAreaId());
        vo.setAreaCode(inventory.getAreaCode());
        vo.setAreaName(inventory.getAreaName());
        vo.setLocationId(inventory.getLocationId());
        vo.setLocationCode(inventory.getLocationCode());
        vo.setLocationName(inventory.getLocationName());
        vo.setMaterialId(inventory.getMaterialId());
        vo.setMaterialCode(inventory.getMaterialCode());
        vo.setMaterialName(inventory.getMaterialName());
        vo.setBatchNo(inventory.getBatchNo());
        vo.setProductionDate(inventory.getProductionDate());
        vo.setExpiryDate(inventory.getExpiryDate());
        vo.setInboundTime(inventory.getInboundTime());
        vo.setQuantity(inventory.getQuantity());
        vo.setLockedQuantity(inventory.getLockedQuantity());
        vo.setFrozenQuantity(inventory.getFrozenQuantity());
        vo.setAvailableQuantity(inventory.getAvailableQuantity());

        Long daysToExpiry = inventory.getExpiryDate() == null
                ? null
                : ChronoUnit.DAYS.between(today, inventory.getExpiryDate());
        vo.setDaysToExpiry(daysToExpiry);
        if (daysToExpiry != null && daysToExpiry < 0) {
            vo.setLevel("EXPIRED");
            vo.setLevelLabel("已过期");
        } else {
            vo.setLevel("EXPIRING_SOON");
            vo.setLevelLabel("临期");
        }
        return vo;
    }

    private void validateStockChange(StockChangeBo change) {
        if (StringUtils.isBlank(change.getBizType())) {
            throw new RuntimeException("库存变动业务类型不能为空");
        }
        if (change.getBizId() == null) {
            throw new RuntimeException("库存变动业务单据ID不能为空");
        }
        if (change.getWarehouseId() == null) {
            throw new RuntimeException("库存变动仓库不能为空");
        }
        if (change.getLocationId() == null) {
            throw new RuntimeException("库存变动库位不能为空");
        }
        if (change.getMaterialId() == null) {
            throw new RuntimeException("库存变动物料不能为空");
        }
        if (change.getChangeQty() == null || change.getChangeQty().compareTo(BigDecimal.ZERO) == 0) {
            throw new RuntimeException("库存变动数量不能为0");
        }
    }

    private void applyInboundChange(StockChangeBo change, Location location) {
        LambdaQueryWrapper<Inventory> lqw = Wrappers.<Inventory>lambdaQuery()
                .eq(Inventory::getWarehouseId, change.getWarehouseId())
                .eq(Inventory::getLocationId, change.getLocationId())
                .eq(Inventory::getMaterialId, change.getMaterialId())
                .eq(Inventory::getBatchNo, normalizeBatchNo(change.getBatchNo()));
        Inventory inventory = inventoryMapper.selectOne(lqw);
        BigDecimal beforeQty = inventory == null || inventory.getQuantity() == null
                ? BigDecimal.ZERO
                : inventory.getQuantity();
        BigDecimal afterQty = beforeQty.add(change.getChangeQty());

        if (inventory == null) {
            Inventory toCreate = new Inventory();
            toCreate.setWarehouseId(change.getWarehouseId());
            toCreate.setLocationId(change.getLocationId());
            toCreate.setMaterialId(change.getMaterialId());
            toCreate.setBatchNo(normalizeBatchNo(change.getBatchNo()));
            toCreate.setProductionDate(change.getProductionDate());
            toCreate.setExpiryDate(change.getExpiryDate());
            toCreate.setInboundTime(change.getOperateTime());
            toCreate.setQuantity(afterQty);
            toCreate.setLockedQuantity(BigDecimal.ZERO);
            toCreate.setFrozenQuantity(BigDecimal.ZERO);
            boolean saved = save(toCreate);
            if (!saved) {
                throw new RuntimeException("库存创建失败");
            }
        } else {
            inventory.setQuantity(afterQty);
            if (inventory.getLockedQuantity() == null) {
                inventory.setLockedQuantity(BigDecimal.ZERO);
            }
            if (inventory.getFrozenQuantity() == null) {
                inventory.setFrozenQuantity(BigDecimal.ZERO);
            }
            if (inventory.getProductionDate() == null) {
                inventory.setProductionDate(change.getProductionDate());
            }
            if (inventory.getExpiryDate() == null) {
                inventory.setExpiryDate(change.getExpiryDate());
            }
            if (inventory.getInboundTime() == null) {
                inventory.setInboundTime(change.getOperateTime());
            }
            boolean updated = updateById(inventory);
            if (!updated) {
                throw new RuntimeException("库存更新失败");
            }
        }

        stockTransactionService.createTransaction(change, location.getAreaId(), beforeQty, afterQty);
    }

    private void applyOutboundChange(StockChangeBo change, Location location) {
        LambdaQueryWrapper<Inventory> lqw = Wrappers.<Inventory>lambdaQuery()
                .eq(Inventory::getWarehouseId, change.getWarehouseId())
                .eq(Inventory::getLocationId, change.getLocationId())
                .eq(Inventory::getMaterialId, change.getMaterialId())
                .eq(Inventory::getBatchNo, normalizeBatchNo(change.getBatchNo()));
        Inventory inventory = inventoryMapper.selectOne(lqw);
        if (inventory == null) {
            throw new RuntimeException(resolveMissingOutboundInventoryMessage(change));
        }

        BigDecimal deductQty = change.getChangeQty().abs();
        int updated = inventoryMapper.deductAvailableQuantity(inventory.getId(), deductQty, resolveOperator());
        if (updated != 1) {
            throw new RuntimeException(resolveOutboundDeductionFailureMessage(change, deductQty, inventory.getId()));
        }

        Inventory latestInventory = inventoryMapper.selectById(inventory.getId());
        if (latestInventory == null || latestInventory.getQuantity() == null) {
            throw new RuntimeException(withLinePrefix(change, "库存扣减成功后未查询到最新库存，请重试"));
        }
        BigDecimal afterQty = latestInventory.getQuantity();
        BigDecimal beforeQty = afterQty.add(deductQty);
        stockTransactionService.createTransaction(change, location.getAreaId(), beforeQty, afterQty);
    }

    private void consumeLockedOutboundChange(StockChangeBo change, Location location) {
        LambdaQueryWrapper<Inventory> lqw = Wrappers.<Inventory>lambdaQuery()
                .eq(Inventory::getWarehouseId, change.getWarehouseId())
                .eq(Inventory::getLocationId, change.getLocationId())
                .eq(Inventory::getMaterialId, change.getMaterialId())
                .eq(Inventory::getBatchNo, normalizeBatchNo(change.getBatchNo()));
        Inventory inventory = inventoryMapper.selectOne(lqw);
        if (inventory == null) {
            throw new RuntimeException(resolveMissingOutboundInventoryMessage(change));
        }

        BigDecimal consumeQty = change.getChangeQty().abs();
        int updated = inventoryMapper.consumeLockedQuantity(inventory.getId(), consumeQty, resolveOperator());
        if (updated != 1) {
            throw new RuntimeException(withLinePrefix(change, "锁定库存不足，无法确认出库"));
        }

        Inventory latestInventory = inventoryMapper.selectById(inventory.getId());
        if (latestInventory == null || latestInventory.getQuantity() == null) {
            throw new RuntimeException(withLinePrefix(change, "锁定库存消费成功后未查询到最新库存，请重试"));
        }
        BigDecimal afterQty = latestInventory.getQuantity();
        BigDecimal beforeQty = afterQty.add(consumeQty);
        stockTransactionService.createTransaction(change, location.getAreaId(), beforeQty, afterQty);
    }

    private String resolveMissingOutboundInventoryMessage(StockChangeBo change) {
        Long locationInventoryCount = inventoryMapper.selectCount(Wrappers.<Inventory>lambdaQuery()
                .eq(Inventory::getWarehouseId, change.getWarehouseId())
                .eq(Inventory::getLocationId, change.getLocationId())
                .gt(Inventory::getQuantity, BigDecimal.ZERO));
        if (locationInventoryCount != null && locationInventoryCount > 0) {
            return withLinePrefix(change, "库位不存在当前物料库存，物料不匹配");
        }
        return withLinePrefix(change, "库位无库存，无法扣减");
    }

    private String resolveOutboundDeductionFailureMessage(StockChangeBo change, BigDecimal deductQty, Long inventoryId) {
        Inventory latestInventory = inventoryMapper.selectById(inventoryId);
        if (latestInventory == null) {
            return withLinePrefix(change, "库存记录不存在，无法扣减");
        }
        BigDecimal quantity = latestInventory.getQuantity() == null ? BigDecimal.ZERO : latestInventory.getQuantity();
        BigDecimal lockedQuantity = latestInventory.getLockedQuantity() == null ? BigDecimal.ZERO : latestInventory.getLockedQuantity();
        BigDecimal frozenQuantity = latestInventory.getFrozenQuantity() == null ? BigDecimal.ZERO : latestInventory.getFrozenQuantity();
        BigDecimal availableQuantity = quantity.subtract(lockedQuantity).subtract(frozenQuantity);
        if (availableQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            return withLinePrefix(change, "库位无可用库存，无法扣减");
        }
        if (availableQuantity.compareTo(deductQty) < 0) {
            return withLinePrefix(change, "库存不足，无法扣减");
        }
        return withLinePrefix(change, "库存扣减失败，请重试");
    }

    private String withLinePrefix(StockChangeBo change, String message) {
        if (change.getLineNo() == null) {
            return message;
        }
        return "行号 " + change.getLineNo() + "：" + message;
    }

    private String normalizeBatchNo(String batchNo) {
        return StringUtils.defaultString(StringUtils.trimToNull(batchNo));
    }

    private Boolean applyFrozenQuantityChange(Long id, StockFreezeBo bo, boolean freeze) {
        if (id == null) {
            throw new RuntimeException("库存ID不能为空");
        }
        validateFreezeBo(bo);
        Inventory inventory = inventoryMapper.selectById(id);
        if (inventory == null) {
            throw new RuntimeException("库存记录不存在");
        }

        BigDecimal changeQty = bo.getQuantity();
        int updated = freeze
                ? inventoryMapper.freezeAvailableQuantity(id, changeQty, resolveOperator())
                : inventoryMapper.unfreezeQuantity(id, changeQty, resolveOperator());
        if (updated != 1) {
            throw new RuntimeException(freeze ? "可用库存不足，无法冻结" : "冻结库存不足，无法解冻");
        }

        Inventory latestInventory = inventoryMapper.selectById(id);
        if (latestInventory == null) {
            throw new RuntimeException("库存冻结状态更新后未查询到最新库存，请重试");
        }

        StockChangeBo transaction = new StockChangeBo();
        transaction.setBizType(freeze ? StockBizTypeConst.STOCK_FREEZE : StockBizTypeConst.STOCK_UNFREEZE);
        transaction.setBizId(id);
        transaction.setWarehouseId(inventory.getWarehouseId());
        transaction.setLocationId(inventory.getLocationId());
        transaction.setMaterialId(inventory.getMaterialId());
        transaction.setBatchNo(inventory.getBatchNo());
        transaction.setProductionDate(inventory.getProductionDate());
        transaction.setExpiryDate(inventory.getExpiryDate());
        transaction.setChangeQty(BigDecimal.ZERO);
        transaction.setRemark(buildFreezeRemark(bo, freeze));

        Location location = locationMapper.selectById(inventory.getLocationId());
        Long areaId = location == null ? null : location.getAreaId();
        BigDecimal beforeQty = normalizeQuantity(inventory.getQuantity());
        BigDecimal afterQty = normalizeQuantity(latestInventory.getQuantity());
        stockTransactionService.createTransaction(transaction, areaId, beforeQty, afterQty);
        return true;
    }

    private void validateFreezeBo(StockFreezeBo bo) {
        if (bo == null || bo.getQuantity() == null || bo.getQuantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("操作数量必须大于0");
        }
        String freezeType = StringUtils.trimToEmpty(bo.getFreezeType()).toUpperCase();
        if (!Set.of(StockFreezeTypeConst.QUALITY, StockFreezeTypeConst.COUNT,
                StockFreezeTypeConst.EXCEPTION, StockFreezeTypeConst.MANUAL).contains(freezeType)) {
            throw new RuntimeException("冻结类型非法: " + bo.getFreezeType());
        }
        bo.setFreezeType(freezeType);
    }

    private String buildFreezeRemark(StockFreezeBo bo, boolean freeze) {
        String action = freeze ? "冻结" : "解冻";
        String reason = StringUtils.defaultString(StringUtils.trimToNull(bo.getReason()), "-");
        return action + "库存，类型：" + bo.getFreezeType() + "，数量：" + bo.getQuantity() + "，原因：" + reason;
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




