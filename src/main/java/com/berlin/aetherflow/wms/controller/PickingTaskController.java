package com.berlin.aetherflow.wms.controller;

import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.wms.domain.bo.PickingTaskActionBo;
import com.berlin.aetherflow.wms.domain.query.PickingTaskQuery;
import com.berlin.aetherflow.wms.service.PickingTaskService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 拣货任务 Controller。
 */
@RestController
@RequestMapping("/api/wms/picking-tasks")
@AllArgsConstructor
public class PickingTaskController {

    private final PickingTaskService pickingTaskService;

    @Operation(summary = "拣货任务分页查询")
    @GetMapping
    public Result<?> page(@ParameterObject PickingTaskQuery query) {
        return Result.success(pickingTaskService.queryList(query));
    }

    @Operation(summary = "拣货任务详情")
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(pickingTaskService.getDetailById(id));
    }

    @Operation(summary = "从出库单生成拣货任务")
    @PostMapping("/from-outbound-order/{outboundOrderId}")
    public Result<?> createFromOutboundOrder(@PathVariable Long outboundOrderId) {
        return Result.success(pickingTaskService.createFromOutboundOrder(outboundOrderId));
    }

    @Operation(summary = "执行拣货任务动作")
    @PostMapping("/{id}/actions")
    public Result<?> applyAction(@PathVariable Long id, @Valid @RequestBody PickingTaskActionBo bo) {
        return Result.success(pickingTaskService.applyAction(id, bo));
    }
}
