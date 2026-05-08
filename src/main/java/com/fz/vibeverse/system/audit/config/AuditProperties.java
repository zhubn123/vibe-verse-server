package com.fz.vibeverse.system.audit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 审计系统配置。
 */
@Data
@ConfigurationProperties(prefix = "vibe-verse.audit")
public class AuditProperties {

    /**
     * 是否启用审计。
     */
    private boolean enabled = true;

    /**
     * 传输方式：async / rocketmq。
     */
    private Transport transport = Transport.ASYNC;

    private Async async = new Async();

    private RocketMq rocketmq = new RocketMq();

    public enum Transport {
        ASYNC,
        ROCKETMQ
    }

    @Data
    public static class Async {
        private int corePoolSize = 2;
        private int maxPoolSize = 4;
        private int queueCapacity = 2000;
        private String threadNamePrefix = "audit-";
    }

    @Data
    public static class RocketMq {
        private String topic = "vibe-verse.audit.topic";
        private String consumerGroup = "vibe-verse-audit-consumer";
    }
}
