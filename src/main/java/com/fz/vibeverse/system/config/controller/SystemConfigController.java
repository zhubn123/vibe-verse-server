package com.fz.vibeverse.system.config.controller;

import com.fz.vibeverse.common.PageResult;
import com.fz.vibeverse.exception.Result;
import com.fz.vibeverse.system.audit.annotation.AuditLog;
import com.fz.vibeverse.system.audit.model.AuditType;
import com.fz.vibeverse.system.config.domain.bo.SystemConfigSaveBo;
import com.fz.vibeverse.system.config.domain.query.SystemConfigQuery;
import com.fz.vibeverse.system.config.domain.vo.SystemConfigVo;
import com.fz.vibeverse.system.config.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统参数配置接口。
 */
@Validated
@RestController
@RequestMapping("/api/system-configs")
@AllArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @Operation(summary = "分页查询系统参数配置")
    @GetMapping
    public Result<PageResult<SystemConfigVo>> queryConfigPage(@Validated SystemConfigQuery query) {
        return Result.success(systemConfigService.queryConfigPage(query));
    }

    @Operation(summary = "查询系统参数配置详情")
    @GetMapping("/{id}")
    public Result<SystemConfigVo> getConfigDetail(@PathVariable Long id) {
        return Result.success(systemConfigService.getConfigDetail(id));
    }

    @Operation(summary = "创建系统参数配置")
    @PostMapping
    @AuditLog(module = "CONFIG", type = AuditType.CREATE, description = "创建系统参数")
    public Result<Void> createConfig(@RequestBody @Valid SystemConfigSaveBo bo) {
        systemConfigService.createConfig(bo);
        return Result.success();
    }

    @Operation(summary = "更新系统参数配置")
    @PutMapping("/{id}")
    @AuditLog(module = "CONFIG", type = AuditType.UPDATE, description = "更新系统参数")
    public Result<Void> updateConfig(@PathVariable Long id, @RequestBody @Valid SystemConfigSaveBo bo) {
        systemConfigService.updateConfig(id, bo);
        return Result.success();
    }

    @Operation(summary = "批量删除系统参数配置")
    @DeleteMapping
    @AuditLog(module = "CONFIG", type = AuditType.DELETE, description = "删除系统参数")
    public Result<Void> deleteConfigs(@RequestParam List<Long> ids) {
        systemConfigService.deleteConfigs(ids);
        return Result.success();
    }
}
