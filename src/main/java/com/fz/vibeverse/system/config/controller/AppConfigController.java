package com.fz.vibeverse.system.config.controller;

import com.fz.vibeverse.exception.Result;
import com.fz.vibeverse.system.config.domain.vo.AppConfigVo;
import com.fz.vibeverse.system.config.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前端应用展示配置接口。
 */
@RestController
@RequestMapping("/api/app-config")
@AllArgsConstructor
public class AppConfigController {

    private final SystemConfigService systemConfigService;

    @Operation(summary = "查询前端应用展示配置")
    @GetMapping
    public Result<AppConfigVo> getAppConfig() {
        return Result.success(systemConfigService.getAppConfig());
    }
}
