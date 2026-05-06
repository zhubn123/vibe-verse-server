package com.berlin.aetherflow.wms.domain.bo;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 上架库位推荐入参。
 */
@Data
public class LocationRecommendPutawayBo {

    @NotNull(message = "仓库不能为空")
    private Long warehouseId;

    private Long areaId;

    @NotNull(message = "物料不能为空")
    private Long materialId;

    @Size(max = 128, message = "批次号长度不能超过128个字符")
    private String batchNo;

    @DecimalMin(value = "0.01", message = "上架数量必须大于0")
    private BigDecimal quantity;

    private Long currentLocationId;

    @Min(value = 1, message = "推荐数量至少为1")
    @Max(value = 20, message = "推荐数量不能超过20")
    private Integer limit;
}
