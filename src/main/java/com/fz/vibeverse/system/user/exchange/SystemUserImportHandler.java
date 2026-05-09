package com.fz.vibeverse.system.user.exchange;

import cn.dev33.satoken.secure.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fz.vibeverse.exception.ApiException;
import com.fz.vibeverse.system.exchange.support.DataImportHandler;
import com.fz.vibeverse.system.exchange.support.DataImportMode;
import com.fz.vibeverse.system.exchange.support.DataImportRow;
import com.fz.vibeverse.system.user.constant.UserConstants;
import com.fz.vibeverse.system.user.domain.entity.SysRole;
import com.fz.vibeverse.system.user.domain.entity.SysUser;
import com.fz.vibeverse.system.user.domain.entity.SysUserRole;
import com.fz.vibeverse.system.user.mapper.SysRoleMapper;
import com.fz.vibeverse.system.user.mapper.SysUserMapper;
import com.fz.vibeverse.system.user.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统用户 CSV 导入处理器。
 */
@Component
@RequiredArgsConstructor
public class SystemUserImportHandler implements DataImportHandler<SystemUserImportHandler.UserImportData> {

    public static final String SCENE = "system-user-import";

    private static final int USER_STATUS_NORMAL = 1;
    private static final DataImportMode IMPORT_MODE = DataImportMode.UPSERT;
    private static final List<String> HEADERS = List.of(
            "username", "nickname", "email", "phone", "password", "roleKeys", "status"
    );
    private static final List<List<String>> SAMPLE_ROWS = List.of(
            List.of("zhangsan", "张三", "zhangsan@example.com", "13800000000", "123456", "operator|viewer", "1")
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
    public List<List<String>> sampleRows() {
        return SAMPLE_ROWS;
    }

    @Override
    public DataImportMode mode() {
        return IMPORT_MODE;
    }

    @Override
    public UserImportData parse(DataImportRow row) {
        String username = normalizeRequired(row.valueAt(0), "用户名不能为空");
        String nickname = StringUtils.defaultIfBlank(normalizeOptional(row.valueAt(1)), username);
        String email = normalizeOptional(row.valueAt(2));
        String phone = normalizeOptional(row.valueAt(3));
        String password = normalizeOptional(row.valueAt(4));
        LinkedHashSet<String> roleKeys = parseRoleKeys(row.valueAt(5));
        Integer status = parseStatus(row.valueAt(6));

        if (username.length() > 64) {
            throw ApiException.badRequest("用户名长度不能超过64位");
        }
        if (nickname.length() > 64) {
            throw ApiException.badRequest("昵称长度不能超过64位");
        }
        if (StringUtils.isNotBlank(email) && email.length() > 128) {
            throw ApiException.badRequest("邮箱长度不能超过128位");
        }
        if (StringUtils.isNotBlank(phone) && phone.length() > 32) {
            throw ApiException.badRequest("手机号长度不能超过32位");
        }
        return new UserImportData(username, nickname, email, phone, password, roleKeys, status);
    }

    @Override
    public String uniqueKey(UserImportData data) {
        return data.username();
    }

    @Override
    public String uniqueKeyName() {
        return "用户名";
    }

    @Override
    public void save(UserImportData data, DataImportMode mode) {
        if (Objects.equals(data.username(), UserConstants.ROOT_ADMIN_USERNAME)) {
            throw ApiException.badRequest("内置管理员 admin 不允许导入变更");
        }

        List<SysRole> roles = resolveActiveRoles(data.roleKeys());
        SysUser existing = sysUserMapper.selectByColumn(SysUser::getUsername, data.username());
        validateMode(data.username(), existing, mode);
        validateImportedEmail(data.email(), existing == null ? null : existing.getId());

        if (existing == null) {
            insertUser(data, roles);
            return;
        }
        updateUser(existing, data, roles);
    }

    private void validateMode(String username, SysUser existing, DataImportMode mode) {
        if (mode == DataImportMode.INSERT_ONLY && existing != null) {
            throw ApiException.badRequest("用户名已存在：" + username);
        }
        if (mode == DataImportMode.UPDATE_ONLY && existing == null) {
            throw ApiException.badRequest("用户名不存在：" + username);
        }
    }

    private void insertUser(UserImportData data, List<SysRole> roles) {
        if (StringUtils.isBlank(data.password()) || data.password().length() < 6) {
            throw ApiException.badRequest("新用户密码不能为空且长度不能小于6位");
        }
        SysUser user = new SysUser();
        user.setUsername(data.username());
        user.setPasswordHash(BCrypt.hashpw(data.password()));
        user.setNickname(data.nickname());
        user.setEmail(data.email());
        user.setPhone(data.phone());
        user.setStatus(data.status());
        user.setLoginFailCount(0);
        user.setLockUntil(null);
        user.setLastLoginTime(null);
        sysUserMapper.insert(user);
        replaceUserRoles(user.getId(), roles);
    }

    private void updateUser(SysUser existing, UserImportData data, List<SysRole> roles) {
        SysUser toUpdate = new SysUser();
        toUpdate.setId(existing.getId());
        toUpdate.setNickname(data.nickname());
        toUpdate.setEmail(data.email());
        toUpdate.setPhone(data.phone());
        toUpdate.setStatus(data.status());
        toUpdate.setLoginFailCount(0);
        toUpdate.setLockUntil(null);
        if (StringUtils.isNotBlank(data.password())) {
            if (data.password().length() < 6) {
                throw ApiException.badRequest("密码长度不能小于6位");
            }
            toUpdate.setPasswordHash(BCrypt.hashpw(data.password()));
        }
        sysUserMapper.updateById(toUpdate);
        replaceUserRoles(existing.getId(), roles);
    }

    private void validateImportedEmail(String email, Long currentUserId) {
        if (StringUtils.isBlank(email)) {
            return;
        }
        SysUser duplicateEmail = sysUserMapper.selectByColumn(SysUser::getEmail, email);
        if (duplicateEmail != null && !Objects.equals(duplicateEmail.getId(), currentUserId)) {
            throw ApiException.business("邮箱已被占用");
        }
    }

    private LinkedHashSet<String> parseRoleKeys(String input) {
        String normalized = normalizeRequired(input, "角色不能为空");
        LinkedHashSet<String> roleKeys = java.util.Arrays.stream(normalized.split("[|;，,]"))
                .map(this::normalizeOptional)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (roleKeys.isEmpty()) {
            throw ApiException.badRequest("角色不能为空");
        }
        return roleKeys;
    }

    private Integer parseStatus(String input) {
        String normalized = StringUtils.defaultIfBlank(normalizeOptional(input), "1");
        if (!"0".equals(normalized) && !"1".equals(normalized)) {
            throw ApiException.badRequest("状态只允许 1 或 0");
        }
        return Integer.parseInt(normalized);
    }

    private List<SysRole> resolveActiveRoles(LinkedHashSet<String> roleKeys) {
        LambdaQueryWrapper<SysRole> roleQuery = new LambdaQueryWrapper<>();
        roleQuery.in(SysRole::getRoleKey, roleKeys);
        List<SysRole> roles = sysRoleMapper.selectList(roleQuery).stream()
                .filter(Objects::nonNull)
                .filter(role -> Objects.equals(role.getStatus(), USER_STATUS_NORMAL))
                .toList();

        Set<String> existingKeys = roles.stream()
                .map(SysRole::getRoleKey)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (existingKeys.size() != roleKeys.size()) {
            LinkedHashSet<String> missingKeys = new LinkedHashSet<>(roleKeys);
            missingKeys.removeAll(existingKeys);
            throw ApiException.business("角色不存在或已停用：" + String.join("、", missingKeys));
        }
        return roles;
    }

    private void replaceUserRoles(Long userId, List<SysRole> roles) {
        LambdaQueryWrapper<SysUserRole> deleteQuery = new LambdaQueryWrapper<>();
        deleteQuery.eq(SysUserRole::getUserId, userId);
        sysUserRoleMapper.delete(deleteQuery);

        for (SysRole role : roles) {
            SysUserRole relation = new SysUserRole();
            relation.setUserId(userId);
            relation.setRoleId(role.getId());
            sysUserRoleMapper.insert(relation);
        }
    }

    private String normalizeRequired(String input, String message) {
        String normalized = normalizeOptional(input);
        if (StringUtils.isBlank(normalized)) {
            throw ApiException.badRequest(message);
        }
        return normalized;
    }

    private String normalizeOptional(String input) {
        return StringUtils.isBlank(input) ? null : input.trim();
    }

    public record UserImportData(
            String username,
            String nickname,
            String email,
            String phone,
            String password,
            LinkedHashSet<String> roleKeys,
            Integer status
    ) {
    }
}
