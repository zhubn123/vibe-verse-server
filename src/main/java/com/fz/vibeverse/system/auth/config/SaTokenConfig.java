package com.fz.vibeverse.system.auth.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.fz.vibeverse.exception.ApiException;
import com.fz.vibeverse.system.user.constant.PermissionConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Locale;
import java.util.Set;

/**
 * Sa-Token 鉴权配置。
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    private static final Set<String> READ_METHODS = Set.of("GET", "HEAD", "OPTIONS");
    private static final String PAGE_SUFFIX = "/page";

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
            SaRouter.match("/api/**")
                    .notMatch(
                            "/api/auth/login",
                            "/api/auth/register",
                            "/api/auth/refresh",
                            "/api/health",
                            "/v3/api-docs",
                            "/v3/api-docs/**",
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/error"
                    )
                    .check(r -> StpUtil.checkLogin());

            SaRouter.match("/api/users/**").check(r -> checkUserPermission());
            SaRouter.match("/api/roles/**").check(r -> checkRolePermission());
            SaRouter.match("/api/permissions/**").check(r -> checkPermissionCatalogPermission());
            SaRouter.match("/api/audit-logs/**").check(r -> checkAuditLogPermission());
            SaRouter.match("/api/dictionaries/**").check(r -> checkDictionaryPermission());
            SaRouter.match("/api/menus/**").check(r -> checkMenuPermission());
            SaRouter.match("/api/system-configs/**").check(r -> checkSystemConfigPermission());
        })).addPathPatterns("/**");
    }

    private void checkMenuPermission() {
        String requestUri = SaHolder.getRequest().getRequestPath();
        String method = SaHolder.getRequest().getMethod();
        if (requestUri == null) {
            return;
        }
        if ("/api/menus/current".equals(requestUri)) {
            return;
        }
        checkResourcePermission(requestUri, method, PermissionConstants.SYSTEM_MENU_VIEW,
                PermissionConstants.SYSTEM_MENU_MANAGE);
    }

    private void checkSystemConfigPermission() {
        String requestUri = SaHolder.getRequest().getRequestPath();
        String method = SaHolder.getRequest().getMethod();
        if (requestUri == null) {
            return;
        }
        checkResourcePermission(requestUri, method, PermissionConstants.SYSTEM_CONFIG_VIEW,
                PermissionConstants.SYSTEM_CONFIG_MANAGE);
    }

    private void checkDictionaryPermission() {
        String requestUri = SaHolder.getRequest().getRequestPath();
        String method = SaHolder.getRequest().getMethod();
        if (requestUri == null) {
            return;
        }
        checkResourcePermission(requestUri, method, PermissionConstants.SYSTEM_DICT_VIEW,
                PermissionConstants.SYSTEM_DICT_MANAGE);
    }

    private void checkPermissionCatalogPermission() {
        String requestUri = SaHolder.getRequest().getRequestPath();
        String method = SaHolder.getRequest().getMethod();
        if (requestUri == null) {
            return;
        }
        checkReadOnlyPermission(requestUri, method, PermissionConstants.SYSTEM_PERMISSION_VIEW);
    }

    private void checkAuditLogPermission() {
        String requestUri = SaHolder.getRequest().getRequestPath();
        String method = SaHolder.getRequest().getMethod();
        if (requestUri == null) {
            return;
        }
        checkReadOnlyPermission(requestUri, method, PermissionConstants.SYSTEM_AUDIT_VIEW);
    }

    private void checkRolePermission() {
        String requestUri = SaHolder.getRequest().getRequestPath();
        String method = SaHolder.getRequest().getMethod();
        if (requestUri == null) {
            return;
        }
        checkResourcePermission(requestUri, method, PermissionConstants.SYSTEM_ROLE_VIEW,
                PermissionConstants.SYSTEM_ROLE_MANAGE);
    }

    private void checkUserPermission() {
        String requestUri = SaHolder.getRequest().getRequestPath();
        String method = SaHolder.getRequest().getMethod();
        if (requestUri == null) {
            return;
        }

        if ("/api/users/profile".equals(requestUri) || "/api/users/password".equals(requestUri)) {
            return;
        }

        if ("/api/users/page".equals(requestUri)) {
            checkReadOnlyPermission(requestUri, method, PermissionConstants.SYSTEM_USER_VIEW);
            return;
        }

        if (requestUri.startsWith("/api/users/")) {
            checkResourcePermission(requestUri, method, PermissionConstants.SYSTEM_USER_VIEW,
                    PermissionConstants.SYSTEM_USER_MANAGE);
            return;
        }

        throw ApiException.forbidden("当前接口尚未配置权限规则，请联系管理员");
    }

    private void checkResourcePermission(String requestUri, String method, String viewPermission, String managePermission) {
        if (isLogicalReadRequest(requestUri, method)) {
            StpUtil.checkPermission(viewPermission);
            return;
        }
        StpUtil.checkPermission(managePermission);
    }

    private void checkReadOnlyPermission(String requestUri, String method, String viewPermission) {
        if (!isLogicalReadRequest(requestUri, method)) {
            throw ApiException.forbidden("当前资源仅允许读取访问");
        }
        StpUtil.checkPermission(viewPermission);
    }

    private boolean isLogicalReadRequest(String requestUri, String method) {
        if (method == null) {
            return false;
        }
        if (READ_METHODS.contains(method.toUpperCase(Locale.ROOT))) {
            return true;
        }
        return "POST".equalsIgnoreCase(method) && requestUri != null && requestUri.endsWith(PAGE_SUFFIX);
    }
}
