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
     * 传输方式：async / mq。
     */
    private Transport transport = Transport.ASYNC;

    private Async async = new Async();

    private Mq mq = new Mq();

    public enum Transport {
        ASYNC,
        MQ
    }

    @Data
    public static class Async {
        private int corePoolSize = 2;
        private int maxPoolSize = 4;
        private int queueCapacity = 2000;
        private String threadNamePrefix = "audit-";
    }

    @Data
    public static class Mq {
        private String exchange = "vibe-verse.audit.exchange";
        private String routingKey = "vibe-verse.audit.log";
        private String queue = "vibe-verse.audit.queue";
    }
}
