package Homepage.practice.Event;

import Homepage.practice.Event.DTO.OrderPaidEvent;
import Homepage.practice.Notification.Notification;
import Homepage.practice.Notification.NotificationRealtimeSender;
import Homepage.practice.Notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidEventConsumer {
    // kafka 이벤트를 읽는 역할
    private final NotificationRepository notificationRepository;
    private final NotificationRealtimeSender notificationRealtimeSender;

    @Transactional
    @KafkaListener(topics = "${app.kafka.topics.order-paid}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(OrderPaidEvent event) {
        log.info("OrderPaidEvent 수신. orderId={}, eventId={}", event.getOrderId(), event.getEventId());

        Optional<Notification> userNotification = saveUserNotification(event);
        Optional<Notification> adminNotification = saveAdminNotification(event);

        // Commit 이후 실시간 알림 전송
        sendAfterCommit(() -> {
            userNotification.ifPresent(notification ->
                    notificationRealtimeSender.sendToUser(event.getUsername(), notification)
            );
            adminNotification.ifPresent(
                    notificationRealtimeSender::sendToAdmins
            );
        });
    }

    private void sendAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            task.run();
                        }
                    }
            );
            return;
        }

        task.run();
    }

    // 유저용 알림
    private Optional<Notification> saveUserNotification(OrderPaidEvent event) {
        String dedupKey = event.getEventId() + ":USER:" + event.getUserId();

        if (notificationRepository.existsByDedupKey(dedupKey)) {
            log.info("사용자 알림 중복 이벤트 무시. dedupKey={}", dedupKey);
            return Optional.empty();
        }

        Notification notification = Notification.createOrderPaidForUser(event.getEventId(), event.getUserId(), event.getOrderId());

        return Optional.of(notificationRepository.save(notification));
    }

    // 관리자용 알림
    private Optional<Notification> saveAdminNotification(OrderPaidEvent event) {
        String dedupKey = event.getEventId() + ":ADMIN";

        if (notificationRepository.existsByDedupKey(dedupKey)) {
            log.info("관리자 알림 중복 이벤트 무시. dedupKey={}", dedupKey);
            return Optional.empty();
        }

        Notification notification = Notification.createOrderPaidForAdmin(event.getEventId(), event.getOrderId());

        return Optional.of(notificationRepository.save(notification));
    }
}
