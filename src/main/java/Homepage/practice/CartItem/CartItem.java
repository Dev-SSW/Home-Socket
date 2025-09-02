package Homepage.practice.CartItem;

import Homepage.practice.Cart.Cart;
import Homepage.practice.Item.Item;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "cart_item_id")
    private Long id;
    private int quantity;          // 장바구니 상품 갯수  order_id : 1 / item_id : 1 / quantity : 2 / cartPrice : 300
    private int cartItemPrice;     // 장바구니 상품 가격  order_id : 1 / item_id : 2 / quantity : 1 / cartPrice : 150

    /** 연관관계 */
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "item_id")
    private Item item;
}
