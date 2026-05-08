package com.fz.vibeverse.system.oss.domain.entity;

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
 * 对象存储元数据实体。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName("oss_object")
public class OssObject extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 逻辑 bucket。
     */
    private String bucket;

    /**
     * 对象 key。
     */
    private String objectKey;

    /**
     * 原始文件名。
     */
    private String originalName;

    /**
     * 文件扩展名。
     */
    private String extension;

    /**
     * MIME 类型。
     */
    private String contentType;

    /**
     * 文件大小，单位字节。
     */
    private Long size;

    /**
     * SHA-256 校验和。
     */
    private String checksumSha256;

    /**
     * 存储实现类型。
     */
    private String storageType;

    /**
     * 底层存储路径。
     */
    private String storagePath;

    /**
     * 访问策略。
     */
    private String accessPolicy;

    /**
     * 状态（1正常 0停用）。
     */
    private Integer status;

    /**
     * 备注。
     */
    private String remark;
}
