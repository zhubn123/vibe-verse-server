package com.fz.vibeverse.system.menu.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fz.vibeverse.exception.ApiException;
import com.fz.vibeverse.system.menu.domain.bo.MenuSaveBo;
import com.fz.vibeverse.system.menu.domain.entity.SysMenu;
import com.fz.vibeverse.system.menu.domain.vo.MenuManageVo;
import com.fz.vibeverse.system.menu.domain.vo.MenuVo;
import com.fz.vibeverse.system.menu.mapper.SysMenuMapper;
import com.fz.vibeverse.system.menu.service.MenuService;
import com.fz.vibeverse.system.user.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
    private static final int STATUS_NORMAL = 1;
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

    @Override
    public List<MenuManageVo> listMenuTree() {
        List<SysMenu> menus = listAllMenus();
        if (menus.isEmpty()) {
            return List.of();
        }
        Map<Long, List<SysMenu>> childrenMap = buildChildrenMap(menus);
        return buildManageTree(ROOT_PARENT_ID, childrenMap);
    }

    @Override
    public MenuManageVo getMenuDetail(Long menuId) {
        return toManageVo(requireMenuById(menuId), List.of());
    }

    @Override
    public void createMenu(MenuSaveBo bo) {
        String menuKey = normalizeMenuKey(bo == null ? null : bo.getMenuKey());
        if (StringUtils.isBlank(menuKey)) {
            throw ApiException.badRequest("菜单标识不能为空");
        }
        if (sysMenuMapper.selectByColumn(SysMenu::getMenuKey, menuKey) != null) {
            throw ApiException.business("菜单标识已存在");
        }

        Long parentId = normalizeParentId(bo == null ? null : bo.getParentId());
        if (parentId > ROOT_PARENT_ID) {
            requireMenuById(parentId);
        }

        SysMenu menu = new SysMenu();
        applyMenu(menu, bo, menuKey, parentId);
        sysMenuMapper.insert(menu);
    }

    @Override
    public void updateMenu(Long menuId, MenuSaveBo bo) {
        SysMenu existing = requireMenuById(menuId);
        String menuKey = normalizeMenuKey(bo == null ? null : bo.getMenuKey());
        if (!Objects.equals(existing.getMenuKey(), menuKey)) {
            throw ApiException.badRequest("菜单标识不允许修改");
        }

        Long parentId = normalizeParentId(bo == null ? null : bo.getParentId());
        if (Objects.equals(parentId, menuId)) {
            throw ApiException.badRequest("父菜单不能选择自己");
        }
        if (parentId > ROOT_PARENT_ID) {
            requireMenuById(parentId);
            ensureNotDescendant(menuId, parentId);
        }

        SysMenu menu = new SysMenu();
        menu.setId(menuId);
        applyMenu(menu, bo, menuKey, parentId);
        sysMenuMapper.updateById(menu);
    }

    @Override
    public void deleteMenus(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw ApiException.badRequest("请选择要删除的菜单");
        }
        if (ids.stream().anyMatch(Objects::isNull)) {
            throw ApiException.badRequest("菜单ID不能为空");
        }

        List<SysMenu> menus = sysMenuMapper.selectByIds(ids).stream()
                .filter(Objects::nonNull)
                .toList();
        if (menus.size() != ids.stream().collect(Collectors.toCollection(LinkedHashSet::new)).size()) {
            throw ApiException.business("存在菜单已被删除或不存在");
        }

        LambdaQueryWrapper<SysMenu> childQuery = new LambdaQueryWrapper<>();
        childQuery.in(SysMenu::getParentId, ids);
        childQuery.notIn(SysMenu::getId, ids);
        if (sysMenuMapper.selectCount(childQuery) > 0) {
            throw ApiException.badRequest("请先删除子菜单");
        }
        sysMenuMapper.deleteByIds(ids);
    }

    private List<MenuVo> buildMenuTree(Long parentId, Map<Long, List<SysMenu>> childrenMap, Set<String> permissionKeys) {
        List<SysMenu> children = childrenMap.getOrDefault(parentId, List.of());
        if (children.isEmpty()) {
            return List.of();
        }

        List<MenuVo> result = new ArrayList<>();
        for (SysMenu menu : children) {
            List<MenuVo> childVos = buildMenuTree(menu.getId(), childrenMap, permissionKeys);
            if (StringUtils.isBlank(menu.getPath()) && childVos.isEmpty()) {
                continue;
            }
            boolean ownAllowed = hasMenuPermission(menu, permissionKeys);
            if (!ownAllowed && childVos.isEmpty()) {
                continue;
            }
            result.add(toVo(menu, childVos));
        }
        return result;
    }

    private List<MenuManageVo> buildManageTree(Long parentId, Map<Long, List<SysMenu>> childrenMap) {
        List<SysMenu> children = childrenMap.getOrDefault(parentId, List.of());
        if (children.isEmpty()) {
            return List.of();
        }
        List<MenuManageVo> result = new ArrayList<>();
        for (SysMenu menu : children) {
            result.add(toManageVo(menu, buildManageTree(menu.getId(), childrenMap)));
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

    private MenuManageVo toManageVo(SysMenu menu, List<MenuManageVo> children) {
        MenuManageVo vo = new MenuManageVo();
        vo.setId(menu.getId());
        vo.setParentId(menu.getParentId());
        vo.setMenuKey(menu.getMenuKey());
        vo.setTitle(menu.getTitle());
        vo.setPath(menu.getPath());
        vo.setIcon(menu.getIcon());
        vo.setPermissionKey(menu.getPermissionKey());
        vo.setSortOrder(menu.getSortOrder());
        vo.setVisible(menu.getVisible());
        vo.setStatus(menu.getStatus());
        vo.setRemark(menu.getRemark());
        vo.setChildren(children);
        return vo;
    }

    private List<SysMenu> listAllMenus() {
        LambdaQueryWrapper<SysMenu> query = new LambdaQueryWrapper<>();
        query.orderByAsc(SysMenu::getParentId, SysMenu::getSortOrder, SysMenu::getId);
        List<SysMenu> menus = sysMenuMapper.selectList(query);
        return menus == null ? List.of() : menus;
    }

    private Map<Long, List<SysMenu>> buildChildrenMap(List<SysMenu> menus) {
        return menus.stream()
                .collect(Collectors.groupingBy(
                        menu -> Objects.requireNonNullElse(menu.getParentId(), ROOT_PARENT_ID),
                        LinkedHashMap::new,
                        Collectors.toCollection(ArrayList::new)
                ));
    }

    private SysMenu requireMenuById(Long menuId) {
        if (menuId == null) {
            throw ApiException.badRequest("菜单ID不能为空");
        }
        SysMenu menu = sysMenuMapper.selectById(menuId);
        if (menu == null) {
            throw ApiException.business("菜单不存在");
        }
        return menu;
    }

    private void ensureNotDescendant(Long menuId, Long parentId) {
        Map<Long, SysMenu> menuMap = listAllMenus().stream()
                .filter(menu -> menu.getId() != null)
                .collect(Collectors.toMap(SysMenu::getId, menu -> menu, (left, right) -> left, LinkedHashMap::new));
        Long currentParentId = parentId;
        while (currentParentId != null && currentParentId > ROOT_PARENT_ID) {
            if (Objects.equals(currentParentId, menuId)) {
                throw ApiException.badRequest("父菜单不能选择当前菜单的子节点");
            }
            SysMenu parent = menuMap.get(currentParentId);
            currentParentId = parent == null ? ROOT_PARENT_ID : parent.getParentId();
        }
    }

    private void applyMenu(SysMenu menu, MenuSaveBo bo, String menuKey, Long parentId) {
        menu.setParentId(parentId);
        menu.setMenuKey(menuKey);
        menu.setTitle(normalizeRequired(bo == null ? null : bo.getTitle(), "菜单标题不能为空"));
        menu.setPath(normalizePath(bo == null ? null : bo.getPath()));
        menu.setIcon(normalizeOptional(bo == null ? null : bo.getIcon()));
        menu.setPermissionKey(normalizeOptional(bo == null ? null : bo.getPermissionKey()));
        menu.setSortOrder(bo == null || bo.getSortOrder() == null ? 0 : bo.getSortOrder());
        menu.setVisible(bo == null || bo.getVisible() == null ? VISIBLE : bo.getVisible());
        menu.setStatus(bo == null || bo.getStatus() == null ? STATUS_NORMAL : bo.getStatus());
        menu.setRemark(StringUtils.defaultString(normalizeOptional(bo == null ? null : bo.getRemark())));
    }

    private Long normalizeParentId(Long parentId) {
        return parentId == null ? ROOT_PARENT_ID : parentId;
    }

    private String normalizeMenuKey(String menuKey) {
        return StringUtils.trimToEmpty(menuKey).toLowerCase();
    }

    private String normalizePath(String path) {
        String normalized = normalizeOptional(path);
        return StringUtils.isBlank(normalized) ? null : normalized;
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
}
