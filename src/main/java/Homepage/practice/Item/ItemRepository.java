package Homepage.practice.Item;

import Homepage.practice.Item.DTO.ItemResponse;
import Homepage.practice.Item.DTO.ItemResponseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    /** 전체 아이템 조회 */
    @Query("select new Homepage.practice.Item.DTO.ItemResponse(i.id, i.name, i.stock, i.itemPrice, i.avgStar) from Item i")
    Page<ItemResponse> findAllItem(Pageable pageable);

    /** 카테고리별 아이템 조회 */
    @Query("select new Homepage.practice.Item.DTO.ItemResponseCategory(i.id, i.name, i.stock, i.itemPrice, i.avgStar, c.id, c.name) " +
            "from Item i join i.category c where c.id = :categoryId")
    Page<ItemResponseCategory> findItemsByCategory(@Param("categoryId") Long categoryId, Pageable pageable);
}
