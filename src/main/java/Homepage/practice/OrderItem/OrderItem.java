package Homepage.practice.OrderItem;

import Homepage.practice.Item.Item;
import Homepage.practice.Order.Order;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "order_item_id")
    private Long id;
    private int quantity;          // 주문 상품 갯수  order_id : 1 / item_id : 1 / quantity : 2 / orderPrice : 300
    private int orderItemPrice;    // 주문 상품 가격  order_id : 1 / item_id : 2 / quantity : 1 / orderPrice : 150

    /** 연관관계 */
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "item_id")
    private Item item;
}
