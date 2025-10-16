package Homepage.practice.Order;

import Homepage.practice.CouponPublish.CouponPublish;
import Homepage.practice.Delivery.Delivery;
import Homepage.practice.Delivery.DeliveryStatus;
import Homepage.practice.Exception.OrderNotCancel;
import Homepage.practice.OrderItem.OrderItem;
import Homepage.practice.User.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column (name ="order_id")
    private Long id;
    private LocalDate orderDate;        // 주문 날짜
    private BigDecimal totalPrice;      // 주문 총 가격

    /** 연관관계 */
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.ALL)
    private Delivery delivery;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "coupon_publish_id")
    private CouponPublish couponPublish;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private User user;

    /** 연관관계 편의 메서드 */
    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
        delivery.setOrder(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void setUser(User user) {
        this.user = user;
    }

    /** 생성 메서드 */
    public static Order createOrder(User user, CouponPublish couponPublish, BigDecimal totalPrice) {
        Order order = Order.builder()
                .couponPublish(couponPublish)
                .orderDate(LocalDate.now())
                .totalPrice(totalPrice)
                .build();
        user.addOrder(order);
        if (couponPublish != null) {
            couponPublish.useCoupon();
        }
        return order;
    }

    /** 비즈니스 로직 */
    public void cancel() {
        if (delivery.getStatus() != DeliveryStatus.READY ) {
            throw new OrderNotCancel("배송 중이거나 완료된 주문은 취소할 수 없습니다.");
        }
        delivery.setStatus(DeliveryStatus.CANCELLED);
        orderItems.forEach(OrderItem::cancel);
        if (couponPublish != null) {
            couponPublish.cancelCoupon();
        }
    }
}
