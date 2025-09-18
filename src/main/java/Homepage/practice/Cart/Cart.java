package Homepage.practice.Cart;

import Homepage.practice.CartItem.CartItem;
import Homepage.practice.User.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "cart_id")
    private Long id;

    /** 연관관계 */
    @OneToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private User user;

    @Builder.Default
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL)
    private List<CartItem> cartItems = new ArrayList<>();

    /** 연관관계 편의 메서드 */
    public void setUser(User user) {
        this.user = user;
    }

    public void addCartItem(CartItem cartItem) {
        cartItems.add(cartItem);
        cartItem.setCart(this);
    }

    /** 생성 메서드 */
    public static Cart createCart(User user) {
        Cart cart = Cart.builder().build();
        user.setCart(cart);
        return cart;
    }

    /** 비즈니스 로직 */
    public int getTotalPrice() {
        return cartItems.stream()
                .mapToInt(CartItem::getTotalPrice)
                .sum();
    }
}
