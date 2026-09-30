package com.thehub.kafka;

import com.thehub.config.KafkaTopicConfig;
import com.thehub.dto.event.NotificationEvent;
import com.thehub.dto.event.OrderEvent;
import com.thehub.dto.event.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class KafkaEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(KafkaEventProducer.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaEventProducer(@org.springframework.beans.factory.annotation.Autowired(required = false) KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderEvent(OrderEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (kafkaTemplate == null) {
            logger.info("KafkaTemplate not configured; skipping publish of OrderEvent {}", event.getEventId());
            return;
        }
        try {
            logger.info("Publishing OrderEvent to Kafka: ID={}, Type={}, OrderId={}",
                    event.getEventId(), event.getEventType(), event.getOrderId());
            kafkaTemplate.send(KafkaTopicConfig.TOPIC_ORDERS, String.valueOf(event.getOrderId()), event);
        } catch (Exception ex) {
            logger.warn("Kafka broker unreachable or publish failed for OrderEvent {}: {}", event.getEventId(), ex.getMessage());
        }
    }

    public void publishPaymentEvent(PaymentEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (kafkaTemplate == null) {
            logger.info("KafkaTemplate not configured; skipping publish of PaymentEvent {}", event.getEventId());
            return;
        }
        try {
            logger.info("Publishing PaymentEvent to Kafka: ID={}, Type={}, OrderId={}",
                    event.getEventId(), event.getEventType(), event.getOrderId());
            kafkaTemplate.send(KafkaTopicConfig.TOPIC_PAYMENTS, String.valueOf(event.getOrderId()), event);
        } catch (Exception ex) {
            logger.warn("Kafka broker unreachable or publish failed for PaymentEvent {}: {}", event.getEventId(), ex.getMessage());
        }
    }

    public void publishNotificationEvent(NotificationEvent event) {
        if (event.getEventId() == null) {
            event.setEventId(UUID.randomUUID().toString());
        }
        if (kafkaTemplate == null) {
            logger.info("KafkaTemplate not configured; skipping publish of NotificationEvent {}", event.getEventId());
            return;
        }
        try {
            logger.info("Publishing NotificationEvent to Kafka: ID={}, Recipient={}",
                    event.getEventId(), event.getRecipientEmail());
            kafkaTemplate.send(KafkaTopicConfig.TOPIC_NOTIFICATIONS, String.valueOf(event.getRecipientUserId()), event);
        } catch (Exception ex) {
            logger.warn("Kafka broker unreachable or publish failed for NotificationEvent {}: {}", event.getEventId(), ex.getMessage());
        }
    }
}
