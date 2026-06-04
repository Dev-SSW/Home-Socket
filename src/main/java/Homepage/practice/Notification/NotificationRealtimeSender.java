package Homepage.practice.Notification;

import Homepage.practice.Notification.DTO.NotificationResponse;
import Homepage.practice.User.Role;
import Homepage.practice.User.User;
import Homepage.practice.User.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationRealtimeSender {
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    public void sendToUser(String username, Notification notification) {
        NotificationResponse response = NotificationResponse.from(notification);

        try {
            messagingTemplate.convertAndSendToUser(username, "/queue/notifications", response);
            log.info("사용자 실시간 알림 전송 완료. username={}, notificationId={}", username, notification.getId());

        } catch (MessageDeliveryException e) {
            log.warn("사용자 실시간 알림 전송 실패. username={}, notificationId={}", username, notification.getId(), e);
        }
    }

    public void sendToAdmins(Notification notification) {
        NotificationResponse response = NotificationResponse.from(notification);

        for (User admin : userRepository.findByRole(Role.ROLE_ADMIN)) {
            try {
                messagingTemplate.convertAndSendToUser(admin.getUsername(), "/queue/admin-notifications", response);
                log.info("관리자 실시간 알림 전송 완료. admin={}, notificationId={}", admin.getUsername(), notification.getId());

            } catch (MessageDeliveryException e) {
                log.warn("관리자 실시간 알림 전송 실패. admin={}, notificationId={}", admin.getUsername(), notification.getId(), e);
            }
        }
    }
}
