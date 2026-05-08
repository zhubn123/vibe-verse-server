package com.fz.vibeverse.system.oss.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.unit.DataSize;

/**
 * 对象存储配置。
 */
@Data
@ConfigurationProperties(prefix = "vibe-verse.oss")
public class OssProperties {

    /**
     * 当前存储实现类型。
     */
    private StorageType storageType = StorageType.LOCAL;

    /**
     * 默认 bucket。
     */
    private String defaultBucket = "attachment";

    /**
     * 单文件最大大小。
     */
    private DataSize maxFileSize = DataSize.ofMegabytes(50);

    /**
     * 本地存储配置。
     */
    private Local local = new Local();

    public enum StorageType {
        LOCAL
    }

    @Data
    public static class Local {

        /**
         * 本地对象根目录。
         */
        private String rootPath;
    }
}
