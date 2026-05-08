package com.fz.vibeverse.system.audit.consumer;

import cn.hutool.json.JSONUtil;
import com.fz.vibeverse.system.audit.model.AuditEvent;
import com.fz.vibeverse.system.audit.persistence.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 审计消费者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "vibe-verse.audit", name = "transport", havingValue = "mq")
public class AuditMqConsumer {

    private final AuditLogMapper auditLogMapper;

    @RabbitListener(queues = "${vibe-verse.audit.mq.queue:vibe-verse.audit.queue}")
    public void consume(String body) {
        try {
            auditLogMapper.insert(JSONUtil.toBean(body, AuditEvent.class));
        } catch (Exception ex) {
            log.error("[AUDIT-MQ] 消费失败, body={}", body, ex);
        }
    }
}
