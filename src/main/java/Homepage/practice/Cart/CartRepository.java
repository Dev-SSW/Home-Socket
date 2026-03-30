package Homepage.practice.Cart;

import Homepage.practice.User.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);

    /** CartItem과 함께 Item 끌어오기 */
    @EntityGraph(attributePaths = {"cartItems", "cartItems.item"})
    @Query("select c from Cart c where c.user.id = :userId")
    Optional<Cart> findCartItemsWithItemByUserId(@Param("userId") Long userId);
}
