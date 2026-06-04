package Homepage.practice.Notification.DTO;

import Homepage.practice.Notification.Notification;
import Homepage.practice.Notification.NotificationReceiverType;
import Homepage.practice.Notification.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponse {
    private Long id;
    private NotificationReceiverType receiverType;
    private Long receiverUserId;
    private NotificationType type;
    private String title;
    private String content;
    private Long relatedOrderId;
    private boolean read;
    private LocalDateTime createdAt;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .receiverType(notification.getReceiverType())
                .receiverUserId(notification.getReceiverUserId())
                .type(notification.getType())
                .title(notification.getTitle())
                .content(notification.getContent())
                .relatedOrderId(notification.getRelatedOrderId())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
