package Homepage.practice.Item;

import Homepage.practice.Item.DTO.ItemResponse;
import Homepage.practice.Item.DTO.ItemResponseCategory;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    /** 전체 아이템 조회 */
    @Query("select new Homepage.practice.Item.DTO.ItemResponse(i.id, i.name, i.stock, i.itemPrice, i.avgStar) from Item i")
    Page<ItemResponse> findAllItem(Pageable pageable);

    /** 카테고리별 아이템 조회 */
    @Query("select new Homepage.practice.Item.DTO.ItemResponseCategory(i.id, i.name, i.stock, i.itemPrice, i.avgStar, c.id, c.name) " +
            "from Item i join i.category c where c.id = :categoryId")
    Page<ItemResponseCategory> findItemsByCategory(@Param("categoryId") Long categoryId, Pageable pageable);

    /** 주문 시 재고 차감을 위한 상품 row lock */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Item i where i.id in :itemIds order by i.id asc")
    List<Item> findAllByIdInForUpdate(@Param("itemIds") List<Long> itemIds);
}
