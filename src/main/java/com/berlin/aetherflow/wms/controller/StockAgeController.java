package com.berlin.aetherflow.wms.controller;

import com.berlin.aetherflow.common.PageResult;
import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.wms.domain.query.StockAgeQuery;
import com.berlin.aetherflow.wms.domain.vo.StockAgeSummaryVo;
import com.berlin.aetherflow.wms.domain.vo.StockAgeVo;
import com.berlin.aetherflow.wms.service.StockAgeService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库龄分析 Controller。
 */
@RestController
@RequestMapping("/api/wms/stock-ages")
@AllArgsConstructor
public class StockAgeController {

    private final StockAgeService stockAgeService;

    @Operation(summary = "库龄分页查询")
    @GetMapping
    public Result<PageResult<StockAgeVo>> page(@Validated @ParameterObject StockAgeQuery query) {
        return Result.success(stockAgeService.queryList(query));
    }

    @Operation(summary = "库龄汇总")
    @GetMapping("/summary")
    public Result<StockAgeSummaryVo> summary(@Validated @ParameterObject StockAgeQuery query) {
        return Result.success(stockAgeService.querySummary(query));
    }
}
