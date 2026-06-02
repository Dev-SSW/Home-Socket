package Homepage.practice.Notification;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(nullable = false, unique = true)
    private String dedupKey;                                // 중복 이벤트 알림 방지

    @Column(nullable = false)
    private String eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationReceiverType receiverType;

    private Long receiverUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    private Long relatedOrderId;

    @Column(nullable = false)
    private boolean isRead;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    /** 이벤트 생성 */
    public static Notification createOrderPaidForUser(String eventId, Long userId, Long orderId) {
        return Notification.builder()
                .dedupKey(eventId + ":USER:" + userId)
                .eventId(eventId)
                .receiverType(NotificationReceiverType.USER)
                .receiverUserId(userId)
                .type(NotificationType.ORDER_PAID)
                .title("결제가 완료되었습니다.")
                .content("주문번호 " + orderId + "번 결제가 완료되었습니다.")
                .relatedOrderId(orderId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public static Notification createOrderPaidForAdmin(String eventId, Long orderId) {
        return Notification.builder()
                .dedupKey(eventId + ":ADMIN")
                .eventId(eventId)
                .receiverType(NotificationReceiverType.ADMIN)
                .receiverUserId(null)
                .type(NotificationType.ORDER_PAID)
                .title("신규 주문이 발생했습니다.")
                .content("주문번호 " + orderId + "번 결제가 완료되었습니다.")
                .relatedOrderId(orderId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /** 알림 확인 여부 */
    public void read() {
        this.isRead = true;
    }
}
