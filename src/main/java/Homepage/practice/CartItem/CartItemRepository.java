package Homepage.practice.CartItem;

import Homepage.practice.Cart.Cart;
import Homepage.practice.Item.Item;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByCart(Cart cart);

    /** 유저의 장바구니 아이템 목록 조회 */
    @Query("select ci from CartItem ci join fetch ci.item i where ci.cart.user.id = :userId")
    List<CartItem> findCartItemsByUserId(@Param("userId") Long userId);
    
    /** cart와 item으로 찾기 */
    @EntityGraph(attributePaths = {"item", "cart"})
    Optional<CartItem> findByCartAndItem(Cart cart, Item item);

    /** 사용자의 특정 CartItem ID들 중 존재하는 ID 목록 조회 */
    @Query("SELECT ci.id FROM CartItem ci WHERE ci.id IN :ids AND ci.cart.user.id = :userId")
    List<Long> findExistingIds(@Param("ids") List<Long> ids, @Param("userId") Long userId);

    /** 사용자의 특정 CartItem들 벌크 삭제 */
    @Modifying
    @Query("DELETE FROM CartItem ci WHERE ci.id IN :ids AND ci.cart.user.id = :userId")
    int deleteByIdsAndUserId(@Param("ids") List<Long> ids, @Param("userId") Long userId);
}
