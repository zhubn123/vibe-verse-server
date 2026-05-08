package com.fz.vibeverse.system.menu.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 菜单管理视图对象。
 */
@Data
public class MenuManageVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "菜单ID")
    private Long id;

    @Schema(description = "父菜单ID")
    private Long parentId;

    @Schema(description = "菜单标识")
    private String menuKey;

    @Schema(description = "菜单标题")
    private String title;

    @Schema(description = "前端路由路径")
    private String path;

    @Schema(description = "图标名称")
    private String icon;

    @Schema(description = "权限码")
    private String permissionKey;

    @Schema(description = "排序号")
    private Integer sortOrder;

    @Schema(description = "是否显示（1显示 0隐藏）")
    private Integer visible;

    @Schema(description = "状态（1正常 0停用）")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "子菜单")
    private List<MenuManageVo> children;
}
