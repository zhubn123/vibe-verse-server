package com.fz.vibeverse.system.exchange.controller;

import com.fz.vibeverse.common.PageResult;
import com.fz.vibeverse.exception.Result;
import com.fz.vibeverse.system.audit.annotation.AuditLog;
import com.fz.vibeverse.system.audit.model.AuditType;
import com.fz.vibeverse.system.exchange.domain.query.DataExchangeTaskQuery;
import com.fz.vibeverse.system.exchange.domain.vo.DataExchangeTaskVo;
import com.fz.vibeverse.system.exchange.service.DataExchangeTaskService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 导入导出任务接口。
 */
@Validated
@RestController
@RequestMapping("/api/data-exchange-tasks")
@AllArgsConstructor
public class DataExchangeTaskController {

    private final DataExchangeTaskService dataExchangeTaskService;

    @Operation(summary = "分页查询导入导出任务")
    @GetMapping
    public Result<PageResult<DataExchangeTaskVo>> queryTaskPage(@Validated DataExchangeTaskQuery query) {
        return Result.success(dataExchangeTaskService.queryTaskPage(query));
    }

    @Operation(summary = "查询导入导出任务详情")
    @GetMapping("/{id}")
    public Result<DataExchangeTaskVo> getTaskDetail(@PathVariable Long id) {
        return Result.success(dataExchangeTaskService.getTaskDetail(id));
    }

    @Operation(summary = "批量删除导入导出任务")
    @DeleteMapping
    @AuditLog(module = "DATA_EXCHANGE", type = AuditType.DELETE, description = "删除导入导出任务")
    public Result<Void> deleteTasks(@RequestParam List<Long> ids) {
        dataExchangeTaskService.deleteTasks(ids);
        return Result.success();
    }
}
