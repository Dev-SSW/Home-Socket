package Homepage.practice.CartItem;

import Homepage.practice.Cart.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart(Cart cart);

    /** 유저의 장바구니 아이템 목록 조회 */
    @Query("select ci from CartItem ci join fetch ci.item i where ci.cart.user.id = :userId")
    List<CartItem> findCartItemsByUserId(@Param("userId") Long userId);
}
