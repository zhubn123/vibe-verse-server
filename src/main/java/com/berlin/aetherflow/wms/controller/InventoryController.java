package com.berlin.aetherflow.wms.controller;

import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.wms.domain.bo.StockFreezeBo;
import com.berlin.aetherflow.wms.domain.query.InventoryExpiryWarningQuery;
import com.berlin.aetherflow.wms.domain.query.InventoryQuery;
import com.berlin.aetherflow.wms.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存 Controller。
 *
 * @author zhubn
 * @date 2026/4/15
 */
@RestController
@RequestMapping("/api/wms/stocks")
@AllArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(summary = "根据ID查询库存")
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(inventoryService.getById(id));
    }

    @Operation(summary = "库存分页查询")
    @GetMapping
    public Result<?> page(@ParameterObject InventoryQuery query) {
        return Result.success(inventoryService.queryList(query));
    }

    @Operation(summary = "库存效期预警")
    @GetMapping("/expiry-warnings")
    public Result<?> expiryWarnings(@Validated @ParameterObject InventoryExpiryWarningQuery query) {
        return Result.success(inventoryService.queryExpiryWarnings(query));
    }

    @Operation(summary = "冻结过期库存")
    @PostMapping("/actions/freeze-expired")
    public Result<?> freezeExpired() {
        return Result.success(inventoryService.freezeExpiredStocks());
    }

    @Operation(summary = "冻结库存")
    @PostMapping("/{id}/freeze")
    public Result<Boolean> freeze(@PathVariable Long id, @Valid @RequestBody StockFreezeBo bo) {
        return Result.success(inventoryService.freezeStock(id, bo));
    }

    @Operation(summary = "解冻库存")
    @PostMapping("/{id}/unfreeze")
    public Result<Boolean> unfreeze(@PathVariable Long id, @Valid @RequestBody StockFreezeBo bo) {
        return Result.success(inventoryService.unfreezeStock(id, bo));
    }
}
