package com.fz.vibeverse.system.audit.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 审计自动配置。
 */
@Configuration
@EnableRabbit
@EnableConfigurationProperties(AuditProperties.class)
public class AuditAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "vibe-verse.audit", name = "transport", havingValue = "mq")
    public DirectExchange auditExchange(AuditProperties properties) {
        return new DirectExchange(properties.getMq().getExchange(), true, false);
    }

    @Bean
    @ConditionalOnProperty(prefix = "vibe-verse.audit", name = "transport", havingValue = "mq")
    public Queue auditQueue(AuditProperties properties) {
        return new Queue(properties.getMq().getQueue(), true);
    }

    @Bean
    @ConditionalOnProperty(prefix = "vibe-verse.audit", name = "transport", havingValue = "mq")
    public Binding auditBinding(DirectExchange auditExchange, Queue auditQueue, AuditProperties properties) {
        return BindingBuilder.bind(auditQueue)
                .to(auditExchange)
                .with(properties.getMq().getRoutingKey());
    }
}
