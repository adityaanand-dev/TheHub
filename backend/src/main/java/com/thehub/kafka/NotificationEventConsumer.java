package com.thehub.kafka;

import com.thehub.config.KafkaTopicConfig;
import com.thehub.dto.event.NotificationEvent;
import com.thehub.entity.Notification;
import com.thehub.entity.User;
import com.thehub.repository.NotificationRepository;
import com.thehub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Profile("!test")
public class NotificationEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationEventConsumer(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @KafkaListener(topics = KafkaTopicConfig.TOPIC_NOTIFICATIONS, groupId = "thehub-notification-processors")
    public void consumeNotificationEvent(NotificationEvent event) {
        logger.info("Kafka Consumer received NotificationEvent: ID={}, RecipientEmail={}, Type={}",
                event.getEventId(), event.getRecipientEmail(), event.getType());

        Optional<User> recipient = Optional.empty();
        if (event.getRecipientUserId() != null) {
            recipient = userRepository.findById(event.getRecipientUserId());
        }
        if (recipient.isEmpty() && event.getRecipientEmail() != null) {
            recipient = userRepository.findByEmail(event.getRecipientEmail().toLowerCase());
        }

        recipient.ifPresent(user -> {
            Notification notification = new Notification(user, event.getTitle(), event.getMessage(), event.getType());
            notificationRepository.save(notification);
            logger.info("Persisted in-app notification #{} for user {}", notification.getId(), user.getEmail());
        });
    }
}
