package com.fz.vibeverse.system.user.exchange;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fz.vibeverse.system.exchange.support.DataExportHandler;
import com.fz.vibeverse.system.user.domain.entity.SysRole;
import com.fz.vibeverse.system.user.domain.entity.SysUser;
import com.fz.vibeverse.system.user.domain.entity.SysUserRole;
import com.fz.vibeverse.system.user.mapper.SysRoleMapper;
import com.fz.vibeverse.system.user.mapper.SysUserMapper;
import com.fz.vibeverse.system.user.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统用户 CSV 导出处理器。
 */
@Component
@RequiredArgsConstructor
public class SystemUserExportHandler implements DataExportHandler<SysUser> {

    public static final String SCENE = "system-user-export";

    private static final int USER_STATUS_NORMAL = 1;
    private static final List<String> HEADERS = List.of(
            "username", "nickname", "email", "phone", "status", "roleKeys", "lastLoginTime"
    );

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    @Override
    public String scene() {
        return SCENE;
    }

    @Override
    public String name() {
        return "用户";
    }

    @Override
    public List<String> headers() {
        return HEADERS;
    }

    @Override
    public List<SysUser> queryData(Map<String, String> params) {
        LambdaQueryWrapper<SysUser> lqw = new LambdaQueryWrapper<>();
        lqw.orderByDesc(SysUser::getCreateTime);

        String username = normalizeOptional(params.get("username"));
        if (StringUtils.isNotBlank(username)) {
            lqw.eq(SysUser::getUsername, username);
        }

        String nickname = normalizeOptional(params.get("nickname"));
        if (StringUtils.isNotBlank(nickname)) {
            lqw.like(SysUser::getNickname, nickname);
        }

        String statusStr = normalizeOptional(params.get("status"));
        if (StringUtils.isNotBlank(statusStr)) {
            try {
                lqw.eq(SysUser::getStatus, Integer.parseInt(statusStr));
            } catch (NumberFormatException ignored) {
            }
        }

        String roleKey = normalizeOptional(params.get("roleKey"));
        if (StringUtils.isNotBlank(roleKey)) {
            Set<Long> userIds = resolveUserIdsByRoleKey(roleKey);
            if (userIds.isEmpty()) {
                return List.of();
            }
            lqw.in(SysUser::getId, userIds);
        }

        return sysUserMapper.selectList(lqw);
    }

    @Override
    public List<?> buildRow(SysUser user) {
        List<String> roles = resolveRoleKeys(user.getId());
        return Arrays.asList(
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus(),
                String.join("|", roles),
                user.getLastLoginTime()
        );
    }

    private List<String> resolveRoleKeys(Long userId) {
        LambdaQueryWrapper<SysUserRole> relationQuery = new LambdaQueryWrapper<>();
        relationQuery.eq(SysUserRole::getUserId, userId);
        List<SysUserRole> relations = sysUserRoleMapper.selectList(relationQuery);
        if (relations == null || relations.isEmpty()) {
            return List.of();
        }

        Set<Long> roleIds = relations.stream()
                .map(SysUserRole::getRoleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (roleIds.isEmpty()) {
            return List.of();
        }

        return sysRoleMapper.selectByIds(roleIds).stream()
                .filter(Objects::nonNull)
                .filter(role -> Objects.equals(role.getStatus(), USER_STATUS_NORMAL))
                .map(SysRole::getRoleKey)
                .filter(StringUtils::isNotBlank)
                .toList();
    }

    private Set<Long> resolveUserIdsByRoleKey(String roleKey) {
        SysRole role = sysRoleMapper.selectByColumn(SysRole::getRoleKey, roleKey);
        if (role == null) {
            return Set.of();
        }

        List<SysUserRole> relations = sysUserRoleMapper.selectListByColumn(SysUserRole::getRoleId, role.getId());
        if (relations == null || relations.isEmpty()) {
            return Set.of();
        }
        return relations.stream()
                .map(SysUserRole::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String normalizeOptional(String input) {
        return StringUtils.isBlank(input) ? null : input.trim();
    }
}
