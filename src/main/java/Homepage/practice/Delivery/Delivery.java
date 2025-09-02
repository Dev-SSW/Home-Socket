package Homepage.practice.Delivery;

import Homepage.practice.Order.Order;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Delivery {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "delivery_id")
    private Long id;

    /** 연관관계 */
    @OneToOne @JoinColumn(name = "order_id")    // 주인이 아닌 쪽만 fetch = FetchType.LAZY 설정
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "address_id")
    private Address address;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;              //READY, SHIPPING, COMPLETE, CANCELLED
}
