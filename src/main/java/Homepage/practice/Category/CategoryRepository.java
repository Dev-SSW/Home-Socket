package Homepage.practice.Category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    /** 특정 부모 카테고리 (계층) */
    List<Category> findByParentId(Long parentId);

    /** 특정 깊이 카테고리 */
    List<Category> findByDepth(int depth);
}
