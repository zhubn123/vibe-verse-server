package com.fz.vibeverse.system.oss.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对象元数据视图。
 */
@Data
public class OssObjectVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String bucket;
    private String objectKey;
    private String originalName;
    private String extension;
    private String contentType;
    private Long size;
    private String checksumSha256;
    private String storageType;
    private String accessPolicy;
    private Integer status;
    private String remark;
    private String downloadUrl;
    private String previewUrl;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
