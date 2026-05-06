package com.berlin.aetherflow.wms.controller;

import com.berlin.aetherflow.common.validation.CreateGroup;
import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.wms.constant.OrderStatusConst;
import com.berlin.aetherflow.wms.domain.bo.TransferOrderActionBo;
import com.berlin.aetherflow.wms.domain.bo.TransferOrderBo;
import com.berlin.aetherflow.wms.domain.query.TransferOrderQuery;
import com.berlin.aetherflow.wms.service.TransferOrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 移库单 Controller。
 */
@RestController
@RequestMapping("/api/wms/transfer-orders")
@AllArgsConstructor
public class TransferOrderController {

    private final TransferOrderService transferOrderService;

    @Operation(summary = "移库单详情查询")
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(transferOrderService.getDetailById(id));
    }

    @Operation(summary = "移库单分页查询")
    @GetMapping
    public Result<?> page(@ParameterObject TransferOrderQuery query) {
        return Result.success(transferOrderService.queryList(query));
    }

    @Operation(summary = "创建移库单草稿")
    @PostMapping
    public Result<?> createDraft(@Validated(CreateGroup.class) @RequestBody TransferOrderBo bo) {
        bo.setId(null);
        bo.setStatus(OrderStatusConst.DRAFT);
        return Result.success(transferOrderService.createTransferOrder(bo));
    }

    @Operation(summary = "编辑移库单")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @Valid @RequestBody TransferOrderBo bo) {
        bo.setId(id);
        return Result.success(transferOrderService.updateTransferOrder(bo));
    }

    @Operation(summary = "执行移库单动作")
    @PostMapping("/{id}/actions")
    public Result<?> applyAction(@PathVariable Long id, @Valid @RequestBody TransferOrderActionBo bo) {
        return Result.success(transferOrderService.applyAction(id, bo));
    }

    @Operation(summary = "批量删除移库单")
    @DeleteMapping
    public Result<?> removeBatch(@RequestParam List<Long> ids) {
        return Result.success(transferOrderService.removeTransferOrders(ids));
    }
}
