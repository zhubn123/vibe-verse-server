package com.fz.vibeverse.system.config.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 前端应用展示配置。
 */
@Data
public class AppConfigVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "平台名称")
    private String platformName;
}
