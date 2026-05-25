package Homepage.practice.Category;

import Homepage.practice.Category.DTO.CategoryHierarchyResponse;
import Homepage.practice.Category.DTO.CategoryRequest;
import Homepage.practice.Category.DTO.CategoryResponse;
import Homepage.practice.Category.DTO.CategoryUpdateRequest;
import Homepage.practice.Common.DTO.CategoryHierarchyListResponse;
import Homepage.practice.Exception.CategoryNotFound;
import Homepage.practice.Exception.CategoryParentNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;

    /** 카테고리 생성 */
    @Transactional
    @CacheEvict(cacheNames = {"getRootCategory", "getChildCategory", "getItemsByCategory"}, allEntries = true)
    public CategoryResponse createCategory(CategoryRequest request) {
        Category parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryParentNotFound("아이디에 해당하는 부모가 없습니다."));
        }
        Category category = Category.createCategory(request, parent);
        categoryRepository.save(category);
        return CategoryResponse.fromEntity(category);
    }
/*
    // 전체 카테고리 정보 가져오기
    public List<CategoryResponse> getAllCategory() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    // 특정 카테고리 정보 가져오기
    public CategoryResponse getCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFound("아이디에 해당하는 카테고리가 없습니다."));
        return CategoryResponse.fromEntity(category);
    }
*/

    /** 루트 카테고리 정보 가져오기 (계층별) */
    @Cacheable(cacheNames = "getRootCategory", key = "'root'")     // 특정 키로 캐싱
    public CategoryHierarchyListResponse getRootCategory() {
        List<CategoryHierarchyResponse> rootCategories =
                categoryRepository.findRootCategoriesWithChildren().stream()
                    .map(CategoryHierarchyResponse::fromEntity)
                    .toList();

        if (rootCategories.isEmpty()) {
            throw new CategoryNotFound("루트 카테고리가 존재하지 않습니다.");
        }

        return new CategoryHierarchyListResponse(rootCategories);
    }

    /** 특정 부모의 자식들 카테고리 가져오기 (계층별) */
    @Cacheable(cacheNames = "getChildCategory", key = "#parentId")
    public CategoryHierarchyListResponse getChildCategory(Long parentId) {
        // 부모 카테고리 존재 여부 확인
        if (!categoryRepository.existsById(parentId)) {
            throw new CategoryNotFound("아이디에 해당하는 부모 카테고리가 없습니다.");
        }

        List<CategoryHierarchyResponse> children =
                categoryRepository.findByParentId(parentId).stream()
                        .map(CategoryHierarchyResponse::fromEntity)
                        .toList();
        return new CategoryHierarchyListResponse(children);
    }

    /** 카테고리 정보 수정하기 */
    @Transactional
    @CacheEvict(cacheNames = {"getRootCategory", "getChildCategory", "getItemsByCategory"}, allEntries = true)
    public CategoryResponse updateCategory(Long categoryId, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFound("아이디에 해당하는 카테고리가 없습니다."));
        category.updateCategory(request);
        return CategoryResponse.fromEntity(category);
    }

    /** 카테고리 삭제하기 */
    @Transactional
    @CacheEvict(cacheNames = {"getRootCategory", "getChildCategory", "getItemsByCategory"}, allEntries = true)
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFound("아이디에 해당하는 카테고리가 없습니다."));
        categoryRepository.delete(category);    // 엔티티 삭제 (cascade + orphanRemoval 작동)
    }
}
