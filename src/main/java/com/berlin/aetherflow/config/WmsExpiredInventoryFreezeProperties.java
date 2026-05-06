package com.berlin.aetherflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * WMS 过期库存自动冻结配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "aether-flow.wms.expired-inventory-freeze")
public class WmsExpiredInventoryFreezeProperties {

    /**
     * 是否启用定时自动冻结。
     */
    private Boolean enabled = true;
}
