package com.fz.vibeverse.system.audit.publisher;

import cn.hutool.json.JSONUtil;
import com.fz.vibeverse.system.audit.config.AuditProperties;
import com.fz.vibeverse.system.audit.model.AuditEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 方案：先发消息，再由消费者异步写库。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "vibe-verse.audit", name = "transport", havingValue = "mq")
public class MqAuditPublisher implements AuditPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final AuditProperties.Mq mq;

    public MqAuditPublisher(RabbitTemplate rabbitTemplate, AuditProperties properties) {
        this.rabbitTemplate = rabbitTemplate;
        this.mq = properties.getMq();
    }

    @Override
    public void publish(AuditEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    mq.getExchange(),
                    mq.getRoutingKey(),
                    JSONUtil.toJsonStr(event),
                    message -> {
                        message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return message;
                    }
            );
        } catch (Exception ex) {
            log.error("[AUDIT-MQ] 发送失败, module={}, type={}", event.getModule(), event.getType(), ex);
        }
    }
}
