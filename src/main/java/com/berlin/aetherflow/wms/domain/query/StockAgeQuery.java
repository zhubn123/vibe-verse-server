package com.berlin.aetherflow.wms.domain.query;

import com.berlin.aetherflow.common.PageQuery;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * 库龄查询对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class StockAgeQuery extends PageQuery {

    /**
     * 仓库ID。
     */
    private Long warehouseId;

    /**
     * 区域ID。
     */
    private Long areaId;

    /**
     * 库位ID。
     */
    private Long locationId;

    /**
     * 物料ID。
     */
    private Long materialId;

    /**
     * 批次号。
     */
    private String batchNo;

    /**
     * 最小库龄天数。
     */
    @Min(value = 0, message = "最小库龄天数不能小于0")
    private Integer minAgeDays;

    /**
     * 最大库龄天数。
     */
    @Min(value = 0, message = "最大库龄天数不能小于0")
    private Integer maxAgeDays;

    /**
     * 统计日期，默认当前日期。
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate asOfDate;

    @AssertTrue(message = "最小库龄天数不能大于最大库龄天数")
    public boolean isAgeRangeValid() {
        return minAgeDays == null || maxAgeDays == null || minAgeDays <= maxAgeDays;
    }

    @AssertTrue(message = "页码必须大于0")
    public boolean isPageNoValid() {
        return getPageNo() != null && getPageNo() > 0;
    }

    @AssertTrue(message = "每页条数必须大于0")
    public boolean isPageSizeValid() {
        return getPageSize() != null && getPageSize() > 0;
    }
}
