package com.fz.vibeverse.system.menu.controller;

import com.fz.vibeverse.exception.Result;
import com.fz.vibeverse.system.audit.annotation.AuditLog;
import com.fz.vibeverse.system.audit.model.AuditType;
import com.fz.vibeverse.system.menu.domain.bo.MenuSaveBo;
import com.fz.vibeverse.system.menu.domain.vo.MenuManageVo;
import com.fz.vibeverse.system.menu.domain.vo.MenuVo;
import com.fz.vibeverse.system.menu.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
 * 当前用户菜单接口。
 */
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
@Validated
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "查询当前用户菜单")
    @GetMapping("/current")
    public Result<List<MenuVo>> listCurrentMenus() {
        return Result.success(menuService.listCurrentMenus());
    }

    @Operation(summary = "查询菜单树")
    @GetMapping
    public Result<List<MenuManageVo>> listMenuTree() {
        return Result.success(menuService.listMenuTree());
    }

    @Operation(summary = "查询菜单详情")
    @GetMapping("/{menuId}")
    public Result<MenuManageVo> getMenuDetail(@PathVariable Long menuId) {
        return Result.success(menuService.getMenuDetail(menuId));
    }

    @Operation(summary = "创建菜单")
    @PostMapping
    @AuditLog(module = "MENU", type = AuditType.CREATE, description = "创建菜单")
    public Result<Void> createMenu(@RequestBody @Valid MenuSaveBo bo) {
        menuService.createMenu(bo);
        return Result.success();
    }

    @Operation(summary = "更新菜单")
    @PutMapping("/{menuId}")
    @AuditLog(module = "MENU", type = AuditType.UPDATE, description = "更新菜单")
    public Result<Void> updateMenu(@PathVariable Long menuId, @RequestBody @Valid MenuSaveBo bo) {
        menuService.updateMenu(menuId, bo);
        return Result.success();
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping
    @AuditLog(module = "MENU", type = AuditType.DELETE, description = "删除菜单")
    public Result<Void> deleteMenus(@RequestParam List<Long> ids) {
        menuService.deleteMenus(ids);
        return Result.success();
    }
}
