package Homepage.practice.Order;

import Homepage.practice.Delivery.Delivery;
import Homepage.practice.OrderItem.OrderItem;
import Homepage.practice.User.User;
import jakarta.persistence.*;
import lombok.*;

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
    private int totalPrice;             // 주문 총 가격

    /** 연관관계 */
    @OneToOne(fetch = FetchType.LAZY, mappedBy = "order", cascade = CascadeType.ALL)
    private Delivery delivery;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

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
}
