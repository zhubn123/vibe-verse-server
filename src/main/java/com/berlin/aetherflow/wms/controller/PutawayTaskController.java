package com.berlin.aetherflow.wms.controller;

import com.berlin.aetherflow.exception.Result;
import com.berlin.aetherflow.wms.domain.bo.PutawayTaskActionBo;
import com.berlin.aetherflow.wms.domain.query.PutawayTaskQuery;
import com.berlin.aetherflow.wms.service.PutawayTaskService;
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
 * 上架任务 Controller。
 */
@RestController
@RequestMapping("/api/wms/putaway-tasks")
@AllArgsConstructor
public class PutawayTaskController {

    private final PutawayTaskService putawayTaskService;

    @Operation(summary = "上架任务分页查询")
    @GetMapping
    public Result<?> page(@ParameterObject PutawayTaskQuery query) {
        return Result.success(putawayTaskService.queryList(query));
    }

    @Operation(summary = "上架任务详情")
    @GetMapping("/{id}")
    public Result<?> getById(@PathVariable Long id) {
        return Result.success(putawayTaskService.getDetailById(id));
    }

    @Operation(summary = "执行上架任务动作")
    @PostMapping("/{id}/actions")
    public Result<?> applyAction(@PathVariable Long id, @Valid @RequestBody PutawayTaskActionBo bo) {
        return Result.success(putawayTaskService.applyAction(id, bo));
    }
}
