package com.fz.vibeverse.system.user.controller;

import com.fz.vibeverse.exception.Result;
import com.fz.vibeverse.system.user.domain.bo.AuthLoginBo;
import com.fz.vibeverse.system.user.domain.bo.AuthRefreshBo;
import com.fz.vibeverse.system.user.domain.bo.AuthRegisterBo;
import com.fz.vibeverse.system.user.domain.vo.AuthLoginVo;
import com.fz.vibeverse.system.user.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口。
 */
@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户注册")
    @SecurityRequirements
    @PostMapping("/register")
    public Result<Long> register(@RequestBody AuthRegisterBo bo) {
        return Result.success(authService.register(bo));
    }

    @Operation(summary = "用户登录")
    @SecurityRequirements
    @PostMapping("/login")
    public Result<AuthLoginVo> login(@RequestBody AuthLoginBo bo, HttpServletRequest request) {
        return Result.success(authService.login(bo, request));
    }

    @Operation(summary = "刷新登录令牌")
    @SecurityRequirements
    @PostMapping("/refresh")
    public Result<AuthLoginVo> refresh(@RequestBody AuthRefreshBo bo, HttpServletRequest request) {
        return Result.success(authService.refresh(bo, request));
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }
}
