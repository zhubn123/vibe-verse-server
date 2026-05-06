package com.berlin.aetherflow.wms.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 上架库位推荐结果。
 */
@Data
public class LocationRecommendationVo {

    private Long locationId;

    private String locationCode;

    private String locationName;

    private Long warehouseId;

    private String warehouseCode;

    private String warehouseName;

    private Long areaId;

    private String areaCode;

    private String areaName;

    private Integer status;

    private Integer score;

    private String reason;

    private BigDecimal materialQuantity;

    private BigDecimal batchQuantity;

    private BigDecimal availableQuantity;

    private Boolean sameMaterial;

    private Boolean sameBatch;

    private Boolean emptyLocation;

    private Boolean currentLocation;
}
