package com.berlin.aetherflow.wms.domain.query;

import com.berlin.aetherflow.common.PageQuery;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 批次追溯分页查询对象。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BatchTraceQuery extends PageQuery {

    /**
     * 仓库ID。
     */
    private Long warehouseId;

    /**
     * 物料ID。
     */
    private Long materialId;

    /**
     * 批次号。传空字符串表示查询空批次。
     */
    private String batchNo;

    /**
     * 生产日期起始。
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate productionDateFrom;

    /**
     * 生产日期截止。
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate productionDateTo;

    /**
     * 到期日期起始。
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expiryDateFrom;

    /**
     * 到期日期截止。
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expiryDateTo;

    /**
     * 最早入库时间起始。
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime inboundTimeFrom;

    /**
     * 最早入库时间截止。
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime inboundTimeTo;

    @AssertTrue(message = "生产日期起始不能晚于截止")
    public boolean isProductionDateRangeValid() {
        return productionDateFrom == null || productionDateTo == null || !productionDateFrom.isAfter(productionDateTo);
    }

    @AssertTrue(message = "到期日期起始不能晚于截止")
    public boolean isExpiryDateRangeValid() {
        return expiryDateFrom == null || expiryDateTo == null || !expiryDateFrom.isAfter(expiryDateTo);
    }

    @AssertTrue(message = "入库时间起始不能晚于截止")
    public boolean isInboundTimeRangeValid() {
        return inboundTimeFrom == null || inboundTimeTo == null || !inboundTimeFrom.isAfter(inboundTimeTo);
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
