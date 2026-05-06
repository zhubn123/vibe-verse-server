package com.berlin.aetherflow.wms.domain.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 出库库存分配预览返回对象。
 */
@Data
@NoArgsConstructor
public class OutboundAllocationPreviewVo {

    private Long orderId;

    private String orderNo;

    private Integer status;

    private Long warehouseId;

    private Boolean sufficient;

    private BigDecimal requiredQty;

    private BigDecimal allocatableQty;

    private BigDecimal shortageQty;

    private List<Item> items;

    @Data
    @NoArgsConstructor
    public static class Item {

        private Long orderItemId;

        private Integer lineNo;

        private Long materialId;

        private String materialCode;

        private String materialName;

        private String batchNo;

        private LocalDate productionDate;

        private LocalDate expiryDate;

        private Long locationId;

        private String locationCode;

        private String locationName;

        private BigDecimal requiredQty;

        private BigDecimal quantity;

        private BigDecimal lockedQuantity;

        private BigDecimal availableQuantity;

        private BigDecimal allocatableQty;

        private BigDecimal shortageQty;

        private Boolean allocatable;

        private String message;

        private List<Segment> segments;
    }

    @Data
    @NoArgsConstructor
    public static class Segment {

        private Long inventoryId;

        private Long locationId;

        private String locationCode;

        private String locationName;

        private String batchNo;

        private LocalDate productionDate;

        private LocalDate expiryDate;

        private BigDecimal availableQuantity;

        private BigDecimal allocatableQty;
    }
}
