package com.fz.vibeverse.system.menu.controller;

import com.fz.vibeverse.exception.Result;
import com.fz.vibeverse.system.menu.domain.vo.MenuVo;
import com.fz.vibeverse.system.menu.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 当前用户菜单接口。
 */
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "查询当前用户菜单")
    @GetMapping("/current")
    public Result<List<MenuVo>> listCurrentMenus() {
        return Result.success(menuService.listCurrentMenus());
    }
}
