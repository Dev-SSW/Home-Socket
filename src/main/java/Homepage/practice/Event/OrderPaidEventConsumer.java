package Homepage.practice.Event;

import Homepage.practice.Event.DTO.OrderPaidEvent;
import Homepage.practice.Notification.Notification;
import Homepage.practice.Notification.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidEventConsumer {
    // kafka 이벤트를 읽는 역할
    private final NotificationRepository notificationRepository;

    @Transactional
    @KafkaListener(topics = "${app.kafka.topics.order-paid}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(OrderPaidEvent event) {
        log.info("OrderPaidEvent 수신. orderId={}, eventId={}", event.getOrderId(), event.getEventId());
        saveUserNotification(event);
        saveAdminNotification(event);
    }

    // 유저용 알림
    private void saveUserNotification(OrderPaidEvent event) {
        String dedupKey = event.getEventId() + ":USER:" + event.getUserId();
        // 이벤트 중복 방지
        if (notificationRepository.existsByDedupKey(dedupKey)) {
            log.info("사용자 알림 중복 이벤트 무시. dedupKey={}", dedupKey);
            return;
        }
        notificationRepository.save(Notification.createOrderPaidForUser(event.getEventId(), event.getUserId(), event.getOrderId()));
    }
    // 관리자용 알림
    private void saveAdminNotification(OrderPaidEvent event) {
        String dedupKey = event.getEventId() + ":ADMIN";

        if (notificationRepository.existsByDedupKey(dedupKey)) {
            log.info("관리자 알림 중복 이벤트 무시. dedupKey={}", dedupKey);
            return;
        }
        notificationRepository.save(Notification.createOrderPaidForAdmin(event.getEventId(), event.getOrderId()));
    }
}
