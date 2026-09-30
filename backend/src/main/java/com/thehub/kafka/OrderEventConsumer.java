package com.thehub.kafka;

import com.thehub.config.KafkaTopicConfig;
import com.thehub.dto.event.NotificationEvent;
import com.thehub.dto.event.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Profile("!test")
public class OrderEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderEventConsumer.class);

    private final KafkaEventProducer kafkaEventProducer;

    public OrderEventConsumer(KafkaEventProducer kafkaEventProducer) {
        this.kafkaEventProducer = kafkaEventProducer;
    }

    @KafkaListener(topics = KafkaTopicConfig.TOPIC_ORDERS, groupId = "thehub-order-processors")
    public void consumeOrderEvent(OrderEvent event) {
        logger.info("Kafka Consumer received OrderEvent: ID={}, Type={}, OrderId={}, Amount=${}",
                event.getEventId(), event.getEventType(), event.getOrderId(), event.getAmount());

        switch (event.getEventType()) {
            case ORDER_CREATED -> {
                logger.info("Processing ORDER_CREATED -> Notifying Freelancer: {}", event.getFreelancerEmail());
                kafkaEventProducer.publishNotificationEvent(new NotificationEvent(
                        UUID.randomUUID().toString(),
                        event.getFreelancerId(),
                        event.getFreelancerEmail(),
                        "New Project Proposal Received",
                        "Client " + event.getClientName() + " has submitted a proposal for: " + event.getServiceTitle(),
                        "ORDER"
                ));
            }
            case ORDER_ACCEPTED -> {
                logger.info("Processing ORDER_ACCEPTED -> Notifying Client: {}", event.getClientEmail());
                kafkaEventProducer.publishNotificationEvent(new NotificationEvent(
                        UUID.randomUUID().toString(),
                        event.getClientId(),
                        event.getClientEmail(),
                        "Your Booking Was Accepted!",
                        "Creator " + event.getCreatorName() + " accepted your booking for: " + event.getServiceTitle(),
                        "ORDER"
                ));
            }
            case ORDER_DECLINED -> {
                logger.info("Processing ORDER_DECLINED -> Notifying Client: {} with reason: {}",
                        event.getClientEmail(), event.getRejectionReason());
                String reasonMsg = event.getRejectionReason() != null ? " Reason: " + event.getRejectionReason() : "";
                kafkaEventProducer.publishNotificationEvent(new NotificationEvent(
                        UUID.randomUUID().toString(),
                        event.getClientId(),
                        event.getClientEmail(),
                        "Booking Request Declined",
                        "Creator " + event.getCreatorName() + " was unable to take your project." + reasonMsg + " Browse alternative creators in TheHub catalog.",
                        "ORDER"
                ));
            }
            case ORDER_COMPLETED -> {
                logger.info("Processing ORDER_COMPLETED -> Order #{} finalized. Prompting client for review.", event.getOrderId());
                kafkaEventProducer.publishNotificationEvent(new NotificationEvent(
                        UUID.randomUUID().toString(),
                        event.getClientId(),
                        event.getClientEmail(),
                        "Project Completed! Leave a Review",
                        "Your project '" + event.getServiceTitle() + "' is completed. Please rate your experience with " + event.getCreatorName() + ".",
                        "REVIEW"
                ));
            }
            default -> logger.info("Handled order lifecycle event: {}", event.getEventType());
        }
    }
}
