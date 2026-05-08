package com.fz.vibeverse.system.user.domain.query;

import com.fz.vibeverse.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 管理端角色分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleManageQuery extends PageQuery {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色标识（精确匹配）
     */
    @Schema(description = "角色标识（精确匹配）")
    @Size(max = 64, message = "角色标识长度不能超过64位")
    private String roleKey;

    /**
     * 角色名称（模糊匹配）
     */
    @Schema(description = "角色名称（模糊匹配）")
    @Size(max = 64, message = "角色名称长度不能超过64位")
    private String roleName;

    /**
     * 状态（1正常 0停用）
     */
    @Schema(description = "状态（1正常 0停用）")
    @Min(value = 0, message = "状态值非法")
    @Max(value = 1, message = "状态值非法")
    private Integer status;
}
