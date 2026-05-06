package com.fz.vibeverse.system.user.controller;

import com.fz.vibeverse.exception.Result;
import com.fz.vibeverse.system.user.domain.vo.PermissionGroupVo;
import com.fz.vibeverse.system.user.service.PermissionCatalogService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 权限目录接口。
 */
@RestController
@RequestMapping("/api/permissions")
@AllArgsConstructor
public class PermissionCatalogController {

    private final PermissionCatalogService permissionCatalogService;

    @Operation(summary = "查询权限目录")
    @GetMapping
    public Result<List<PermissionGroupVo>> listPermissionCatalog() {
        return Result.success(permissionCatalogService.listPermissionCatalog());
    }
}
