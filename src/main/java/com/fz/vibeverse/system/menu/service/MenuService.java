package com.fz.vibeverse.system.menu.service;

import com.fz.vibeverse.system.menu.domain.bo.MenuSaveBo;
import com.fz.vibeverse.system.menu.domain.vo.MenuManageVo;
import com.fz.vibeverse.system.menu.domain.vo.MenuVo;

import java.util.List;

/**
 * 系统菜单服务。
 */
public interface MenuService {

    /**
     * 查询当前用户可见菜单。
     *
     * @return 菜单树
     */
    List<MenuVo> listCurrentMenus();

    /**
     * 查询菜单树。
     *
     * @return 管理端菜单树
     */
    List<MenuManageVo> listMenuTree();

    /**
     * 查询菜单详情。
     *
     * @param menuId 菜单ID
     * @return 菜单详情
     */
    MenuManageVo getMenuDetail(Long menuId);

    /**
     * 创建菜单。
     *
     * @param bo 保存参数
     */
    void createMenu(MenuSaveBo bo);

    /**
     * 更新菜单。
     *
     * @param menuId 菜单ID
     * @param bo 保存参数
     */
    void updateMenu(Long menuId, MenuSaveBo bo);

    /**
     * 删除菜单。
     *
     * @param ids 菜单ID集合
     */
    void deleteMenus(List<Long> ids);
}
