package com.berlin.aetherflow.wms.controller;

import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.wms.domain.bo.StockCountActionBo;
import com.berlin.aetherflow.wms.domain.bo.StockCountCreateBo;
import com.berlin.aetherflow.wms.domain.bo.StockCountItemsBo;
import com.berlin.aetherflow.wms.domain.bo.StockCountReviewItemsBo;
import com.berlin.aetherflow.wms.domain.query.StockCountQuery;
import com.berlin.aetherflow.wms.service.StockCountService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 盘点单 Controller。
 */
@RestController
@RequestMapping("/api/wms/stock-counts")
@AllArgsConstructor
public class StockCountController {

    private final StockCountService stockCountService;

    @Operation(summary = "盘点单分页查询")
    @GetMapping
    public Result<?> page(@ParameterObject StockCountQuery query) {
        return Result.success(stockCountService.queryList(query));
    }

    @Operation(summary = "盘点单详情")
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(stockCountService.getDetailById(id));
    }

    @Operation(summary = "创建盘点单")
    @PostMapping
    public Result<?> create(@Valid @RequestBody StockCountCreateBo bo) {
        return Result.success(stockCountService.createStockCount(bo));
    }

    @Operation(summary = "保存盘点明细实盘数量")
    @PutMapping("/{id}/items")
    public Result<?> saveItems(@PathVariable Long id, @Valid @RequestBody StockCountItemsBo bo) {
        return Result.success(stockCountService.saveCountItems(id, bo));
    }

    @Operation(summary = "保存盘点复盘明细")
    @PutMapping("/{id}/review-items")
    public Result<?> saveReviewItems(@PathVariable Long id, @Valid @RequestBody StockCountReviewItemsBo bo) {
        return Result.success(stockCountService.saveReviewItems(id, bo));
    }

    @Operation(summary = "执行盘点单动作")
    @PostMapping("/{id}/actions")
    public Result<?> applyAction(@PathVariable Long id, @Valid @RequestBody StockCountActionBo bo) {
        return Result.success(stockCountService.applyAction(id, bo));
    }
}
