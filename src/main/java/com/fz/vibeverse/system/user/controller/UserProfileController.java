package com.fz.vibeverse.system.user.controller;

import com.fz.vibeverse.exception.Result;
import com.fz.vibeverse.system.user.domain.bo.UserPasswordUpdateBo;
import com.fz.vibeverse.system.user.domain.bo.UserProfileUpdateBo;
import com.fz.vibeverse.system.user.domain.vo.UserProfileVo;
import com.fz.vibeverse.system.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户资料接口。
 */
@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @Operation(summary = "获取当前用户资料")
    @GetMapping("/profile")
    public Result<UserProfileVo> getProfile() {
        return Result.success(userProfileService.getCurrentProfile());
    }

    @Operation(summary = "更新当前用户资料")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody UserProfileUpdateBo bo, HttpServletRequest request) {
        userProfileService.updateCurrentProfile(bo, request);
        return Result.success();
    }

    @Operation(summary = "修改当前用户密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody UserPasswordUpdateBo bo, HttpServletRequest request) {
        userProfileService.updateCurrentPassword(bo, request);
        return Result.success();
    }
}
