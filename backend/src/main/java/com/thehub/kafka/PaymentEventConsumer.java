package com.thehub.kafka;

import com.thehub.config.KafkaTopicConfig;
import com.thehub.dto.event.PaymentEvent;
import com.thehub.entity.Order;
import com.thehub.entity.OrderStatus;
import com.thehub.entity.Payment;
import com.thehub.repository.OrderRepository;
import com.thehub.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Profile("!test")
public class PaymentEventConsumer {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentEventConsumer(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @KafkaListener(topics = KafkaTopicConfig.TOPIC_PAYMENTS, groupId = "thehub-payment-processors")
    public void consumePaymentEvent(PaymentEvent event) {
        logger.info("Kafka Consumer received PaymentEvent: ID={}, Type={}, OrderId={}, Amount=${}",
                event.getEventId(), event.getEventType(), event.getOrderId(), event.getAmount());

        Optional<Order> orderOpt = orderRepository.findById(event.getOrderId());
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            Payment payment = paymentRepository.findByOrderId(order.getId())
                    .orElse(new Payment(order, event.getAmount(), event.getEventType().name(), event.getTransactionId()));

            payment.setStatus(event.getEventType().name());
            paymentRepository.save(payment);

            if (event.getEventType() == PaymentEvent.EventType.PAYMENT_SUCCESS && order.getStatus() == OrderStatus.Delivered) {
                order.setStatus(OrderStatus.Completed);
                orderRepository.save(order);
                logger.info("Order #{} marked as Completed after payment settlement.", order.getId());
            }
        }
    }
}
