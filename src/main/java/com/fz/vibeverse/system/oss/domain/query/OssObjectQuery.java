package com.fz.vibeverse.system.oss.domain.query;

import com.fz.vibeverse.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 对象元数据分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OssObjectQuery extends PageQuery {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "bucket")
    @Size(max = 64, message = "bucket 长度不能超过64位")
    @Pattern(regexp = "^[a-z0-9][a-z0-9_-]{0,63}$|^$", message = "bucket 格式非法")
    private String bucket;

    @Schema(description = "原始文件名")
    @Size(max = 255, message = "文件名长度不能超过255位")
    private String originalName;

    @Schema(description = "文件类型")
    @Size(max = 128, message = "文件类型长度不能超过128位")
    private String contentType;

    @Schema(description = "状态（1正常 0停用）")
    @Min(value = 0, message = "状态值非法")
    @Max(value = 1, message = "状态值非法")
    private Integer status;
}
