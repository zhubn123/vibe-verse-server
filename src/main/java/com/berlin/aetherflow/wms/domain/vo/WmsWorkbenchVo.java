package com.berlin.aetherflow.wms.domain.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * WMS workbench overview.
 */
@Data
@NoArgsConstructor
public class WmsWorkbenchVo {

    private Summary summary = new Summary();

    private List<WarehouseOption> warehouses = new ArrayList<>();

    private List<WarningRow> warnings = new ArrayList<>();

    private List<Activity> activities = new ArrayList<>();

    private List<TrendPoint> trends = new ArrayList<>();

    @Data
    @NoArgsConstructor
    public static class Summary {

        private Long warningCount = 0L;

        private Long exceptionCount = 0L;

        private Long todayInboundCount = 0L;

        private Long yesterdayInboundCount = 0L;

        private Long todayOutboundCount = 0L;

        private Long yesterdayOutboundCount = 0L;

        private BigDecimal stockQuantity = BigDecimal.ZERO;

        private Long stockedMaterialCount = 0L;
    }

    @Data
    @NoArgsConstructor
    public static class WarehouseOption {

        private Long id;

        private String warehouseCode;

        private String warehouseName;
    }

    @Data
    @NoArgsConstructor
    public static class WarningRow {

        private Long inventoryId;

        private Long warehouseId;

        private String warehouseName;

        private String locationCode;

        private String materialCode;

        private String materialName;

        private String specification;

        private BigDecimal stock = BigDecimal.ZERO;

        private String level;

        private String levelLabel;
    }

    @Data
    @NoArgsConstructor
    public static class Activity {

        private String time;

        private String title;

        private String user;

        private String type;
    }

    @Data
    @NoArgsConstructor
    public static class TrendPoint {

        private String date;

        private String label;

        private Long inboundCount = 0L;

        private Long outboundCount = 0L;
    }
}
