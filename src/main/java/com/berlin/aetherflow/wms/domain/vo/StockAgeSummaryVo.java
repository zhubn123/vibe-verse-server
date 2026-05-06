package com.berlin.aetherflow.wms.domain.vo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 库龄分析汇总返回对象。
 */
@Data
@NoArgsConstructor
public class StockAgeSummaryVo {

    private LocalDate asOfDate;

    private Long totalCount;

    private BigDecimal totalQuantity;

    private BigDecimal totalAvailableQuantity;

    private Long knownAgeCount;

    private BigDecimal knownAgeQuantity;

    private Long unknownAgeCount;

    private BigDecimal unknownAgeQuantity;

    private Long age0To30Count;

    private BigDecimal age0To30Quantity;

    private Long age31To60Count;

    private BigDecimal age31To60Quantity;

    private Long age61To90Count;

    private BigDecimal age61To90Quantity;

    private Long ageOver90Count;

    private BigDecimal ageOver90Quantity;
}
