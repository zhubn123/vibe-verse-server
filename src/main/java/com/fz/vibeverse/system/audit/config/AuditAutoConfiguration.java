package com.fz.vibeverse.system.audit.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 审计自动配置。
 */
@Configuration
@EnableConfigurationProperties(AuditProperties.class)
public class AuditAutoConfiguration {
}
