package com.fz.vibeverse.system.reminder.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 提醒保存参数。
 */
@Data
public class ReminderSaveBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "提醒标题")
    @NotBlank(message = "提醒标题不能为空")
    @Size(max = 128, message = "提醒标题长度不能超过128位")
    private String title;

    @Schema(description = "提醒内容")
    @Size(max = 1000, message = "提醒内容长度不能超过1000位")
    private String content;

    @Schema(description = "提醒时间")
    @NotNull(message = "提醒时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime remindTime;

    @Schema(description = "备注")
    @Size(max = 255, message = "备注长度不能超过255位")
    private String remark;
}
