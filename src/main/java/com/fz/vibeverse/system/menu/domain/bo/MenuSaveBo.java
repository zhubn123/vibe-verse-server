package com.fz.vibeverse.system.menu.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 菜单保存参数。
 */
@Data
public class MenuSaveBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "父菜单ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "父菜单ID不能为空")
    private Long parentId;

    @Schema(description = "菜单标识", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "菜单标识不能为空")
    @Size(max = 128, message = "菜单标识长度不能超过128位")
    @Pattern(regexp = "^[a-z][a-z0-9-]{0,127}$", message = "菜单标识必须以小写字母开头，仅支持小写字母、数字和短横线")
    private String menuKey;

    @Schema(description = "菜单标题", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "菜单标题不能为空")
    @Size(max = 64, message = "菜单标题长度不能超过64位")
    private String title;

    @Schema(description = "前端路由路径")
    @Size(max = 255, message = "路由路径长度不能超过255位")
    @Pattern(regexp = "^$|^/[A-Za-z0-9/_-]*$", message = "路由路径格式非法")
    private String path;

    @Schema(description = "图标名称")
    @Size(max = 64, message = "图标名称长度不能超过64位")
    @Pattern(regexp = "^$|^[A-Za-z][A-Za-z0-9]*$", message = "图标名称格式非法")
    private String icon;

    @Schema(description = "权限码")
    @Size(max = 128, message = "权限码长度不能超过128位")
    @Pattern(regexp = "^$|^[a-z][a-z0-9:_-]{0,127}$", message = "权限码格式非法")
    private String permissionKey;

    @Schema(description = "排序号", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "排序号不能为空")
    private Integer sortOrder;

    @Schema(description = "是否显示（1显示 0隐藏）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "显示状态不能为空")
    @Min(value = 0, message = "显示状态值非法")
    @Max(value = 1, message = "显示状态值非法")
    private Integer visible;

    @Schema(description = "状态（1正常 0停用）", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "状态不能为空")
    @Min(value = 0, message = "状态值非法")
    @Max(value = 1, message = "状态值非法")
    private Integer status;

    @Schema(description = "备注")
    @Size(max = 255, message = "备注长度不能超过255位")
    private String remark;
}
