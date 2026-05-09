package com.fz.vibeverse.system.reminder.domain.query;

import com.fz.vibeverse.common.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 提醒分页查询参数。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ReminderQuery extends PageQuery {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "标题")
    @Size(max = 128, message = "标题长度不能超过128位")
    private String title;

    @Schema(description = "状态")
    @Pattern(regexp = "^(PENDING|DONE|CANCELLED)?$", message = "提醒状态非法")
    private String status;

    @Schema(description = "是否只看已到期提醒")
    private Boolean dueOnly;
}
