package com.fz.vibeverse.system.menu.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fz.vibeverse.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 系统菜单实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class SysMenu extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 父菜单 ID，根菜单为 0。
     */
    private Long parentId;

    /**
     * 菜单唯一标识。
     */
    private String menuKey;

    /**
     * 菜单标题。
     */
    private String title;

    /**
     * 前端路由路径。
     */
    private String path;

    /**
     * 前端图标名称。
     */
    private String icon;

    /**
     * 访问该菜单需要的权限码。
     */
    private String permissionKey;

    /**
     * 排序号。
     */
    private Integer sortOrder;

    /**
     * 是否显示（1显示 0隐藏）。
     */
    private Integer visible;

    /**
     * 状态（0正常 1停用）。
     */
    private Integer status;

    /**
     * 备注。
     */
    private String remark;
}
