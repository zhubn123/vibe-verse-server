package com.fz.vibeverse.system.exchange.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 导入导出任务视图。
 */
@Data
public class DataExchangeTaskVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String direction;
    private String scene;
    private String status;
    private Long sourceObjectId;
    private Long resultObjectId;
    private Long errorObjectId;
    private Integer totalCount;
    private Integer successCount;
    private Integer failCount;
    private String message;
    private String remark;
    private String sourceDownloadUrl;
    private String resultDownloadUrl;
    private String errorDownloadUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime finishTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
