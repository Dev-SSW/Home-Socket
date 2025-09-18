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
    //private int cartItemPrice;   // 장바구니 상품 가격  order_id : 1 / item_id : 2 / quantity : 1 / cartPrice : 150

    /** 연관관계 */
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "cart_id")
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "item_id")
    private Item item;

    /** 연관관계 편의 메서드 */
    public void setCart(Cart cart) { this.cart = cart; }

    public void setItem(Item item) {
        this.item = item;
    }

    /** 생성 메서드 */
    public static CartItem createCartItem(Cart cart, Item item, int quantity) {
        CartItem cartItem = CartItem.builder()
                .quantity(quantity)
                .build();
        cart.addCartItem(cartItem);
        item.addCartItem(cartItem);
        return cartItem;
    }
    /** 비즈니스 로직 */
    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }

    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getTotalPrice() {
        return this.item.getItemPrice() * this.quantity;
    }
}
