package com.berlin.aetherflow.wms.support;

import com.berlin.aetherflow.config.WmsExpiredInventoryFreezeProperties;
import com.berlin.aetherflow.wms.domain.vo.ExpiredInventoryFreezeResultVo;
import com.berlin.aetherflow.wms.service.InventoryService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 过期库存自动冻结任务。
 */
@Slf4j
@Component
@AllArgsConstructor
public class ExpiredInventoryFreezeScheduler {

    private final InventoryService inventoryService;
    private final WmsExpiredInventoryFreezeProperties properties;

    @Scheduled(cron = "${aether-flow.wms.expired-inventory-freeze.cron:0 15 1 * * *}")
    public void freezeExpiredInventory() {
        if (!Boolean.TRUE.equals(properties.getEnabled())) {
            return;
        }

        ExpiredInventoryFreezeResultVo result = inventoryService.freezeExpiredStocks();
        log.info("Expired inventory freeze finished, scanned={}, frozen={}, quantity={}, skipped={}, failed={}",
                result.getScannedCount(),
                result.getFrozenCount(),
                result.getFrozenQuantity(),
                result.getSkippedCount(),
                result.getFailedCount());
    }
}
