package com.fz.vibeverse.system.audit.publisher;

import cn.hutool.json.JSONUtil;
import com.fz.vibeverse.system.audit.config.AuditProperties;
import com.fz.vibeverse.system.audit.model.AuditEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * RocketMQ 方案：先发消息，再由消费者异步写库。
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "vibe-verse.audit", name = "transport", havingValue = "rocketmq")
public class RocketMqAuditPublisher implements AuditPublisher {

    private final RocketMQTemplate rocketMQTemplate;
    private final AuditProperties.RocketMq rocketmq;

    public RocketMqAuditPublisher(RocketMQTemplate rocketMQTemplate, AuditProperties properties) {
        this.rocketMQTemplate = rocketMQTemplate;
        this.rocketmq = properties.getRocketmq();
    }

    @Override
    public void publish(AuditEvent event) {
        try {
            rocketMQTemplate.syncSend(rocketmq.getTopic(), JSONUtil.toJsonStr(event));
        } catch (Exception ex) {
            log.error("[AUDIT-ROCKETMQ] 发送失败, module={}, type={}", event.getModule(), event.getType(), ex);
        }
    }
}
