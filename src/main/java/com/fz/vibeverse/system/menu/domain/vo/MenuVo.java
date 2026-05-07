package com.fz.vibeverse.system.menu.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 当前用户菜单返回对象。
 */
@Data
public class MenuVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单 ID")
    private Long id;

    @Schema(description = "父菜单 ID")
    private Long parentId;

    @Schema(description = "菜单标识")
    private String menuKey;

    @Schema(description = "菜单标题")
    private String title;

    @Schema(description = "前端路由路径")
    private String path;

    @Schema(description = "前端图标名称")
    private String icon;

    @Schema(description = "权限码")
    private String permissionKey;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "子菜单")
    private List<MenuVo> children;
}
