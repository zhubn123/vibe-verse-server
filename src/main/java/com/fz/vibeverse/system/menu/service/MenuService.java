package com.fz.vibeverse.system.menu.service;

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
}
