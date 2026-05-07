package com.fz.vibeverse.system.menu.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fz.vibeverse.system.menu.domain.entity.SysMenu;
import com.fz.vibeverse.system.menu.domain.vo.MenuVo;
import com.fz.vibeverse.system.menu.mapper.SysMenuMapper;
import com.fz.vibeverse.system.menu.service.MenuService;
import com.fz.vibeverse.system.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统菜单服务实现。
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private static final long ROOT_PARENT_ID = 0L;
    private static final int STATUS_NORMAL = 0;
    private static final int VISIBLE = 1;

    private final SysMenuMapper sysMenuMapper;
    private final AuthService authService;

    @Override
    public List<MenuVo> listCurrentMenus() {
        Long userId = StpUtil.getLoginIdAsLong();
        Set<String> permissionKeys = authService.getPermissionKeysByUserId(userId).stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());

        LambdaQueryWrapper<SysMenu> query = new LambdaQueryWrapper<>();
        query.eq(SysMenu::getStatus, STATUS_NORMAL);
        query.eq(SysMenu::getVisible, VISIBLE);
        query.orderByAsc(SysMenu::getParentId, SysMenu::getSortOrder, SysMenu::getId);
        List<SysMenu> menus = sysMenuMapper.selectList(query);
        if (menus == null || menus.isEmpty()) {
            return List.of();
        }

        Map<Long, List<SysMenu>> childrenMap = menus.stream()
                .collect(Collectors.groupingBy(
                        menu -> Objects.requireNonNullElse(menu.getParentId(), ROOT_PARENT_ID),
                        LinkedHashMap::new,
                        Collectors.toCollection(ArrayList::new)
                ));

        return buildMenuTree(ROOT_PARENT_ID, childrenMap, permissionKeys);
    }

    private List<MenuVo> buildMenuTree(Long parentId, Map<Long, List<SysMenu>> childrenMap, Set<String> permissionKeys) {
        List<SysMenu> children = childrenMap.getOrDefault(parentId, List.of());
        if (children.isEmpty()) {
            return List.of();
        }

        List<MenuVo> result = new ArrayList<>();
        for (SysMenu menu : children) {
            List<MenuVo> childVos = buildMenuTree(menu.getId(), childrenMap, permissionKeys);
            boolean ownAllowed = hasMenuPermission(menu, permissionKeys);
            if (!ownAllowed && childVos.isEmpty()) {
                continue;
            }
            result.add(toVo(menu, childVos));
        }
        return result;
    }

    private boolean hasMenuPermission(SysMenu menu, Set<String> permissionKeys) {
        if (menu == null || StringUtils.isBlank(menu.getPermissionKey())) {
            return true;
        }
        return permissionKeys.contains(menu.getPermissionKey());
    }

    private MenuVo toVo(SysMenu menu, List<MenuVo> children) {
        MenuVo vo = new MenuVo();
        vo.setId(menu.getId());
        vo.setParentId(menu.getParentId());
        vo.setMenuKey(menu.getMenuKey());
        vo.setTitle(menu.getTitle());
        vo.setPath(menu.getPath());
        vo.setIcon(menu.getIcon());
        vo.setPermissionKey(menu.getPermissionKey());
        vo.setSortOrder(menu.getSortOrder());
        vo.setChildren(children);
        return vo;
    }
}
