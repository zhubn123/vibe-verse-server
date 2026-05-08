package com.fz.vibeverse.system.audit.consumer;

import cn.hutool.json.JSONUtil;
import com.fz.vibeverse.system.audit.model.AuditEvent;
import com.fz.vibeverse.system.audit.persistence.AuditLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * RocketMQ 审计消费者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "vibe-verse.audit", name = "transport", havingValue = "rocketmq")
@RocketMQMessageListener(
        topic = "${vibe-verse.audit.rocketmq.topic:vibe-verse.audit.topic}",
        consumerGroup = "${vibe-verse.audit.rocketmq.consumer-group:vibe-verse-audit-consumer}",
        consumeMode = ConsumeMode.CONCURRENTLY
)
public class AuditRocketMqConsumer implements RocketMQListener<String> {

    private final AuditLogMapper auditLogMapper;

    @Override
    public void onMessage(String body) {
        try {
            auditLogMapper.insert(JSONUtil.toBean(body, AuditEvent.class));
        } catch (Exception ex) {
            log.error("[AUDIT-ROCKETMQ] 消费失败, body={}", body, ex);
        }
    }
}
