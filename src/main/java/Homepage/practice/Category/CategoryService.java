package Homepage.practice.Category;

import Homepage.practice.Category.DTO.CategoryRequest;
import Homepage.practice.Category.DTO.CategoryResponse;
import Homepage.practice.Category.DTO.CategoryUpdateRequest;
import Homepage.practice.Exception.CategoryNotFound;
import Homepage.practice.Exception.CategoryParentNotFound;
import lombok.RequiredArgsConstructor;
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

    /** 전체 카테고리 정보 가져오기 */
    public List<CategoryResponse> getAllCategory() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    /** 특정 카테고리 정보 가져오기 */
    public CategoryResponse getCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFound("아이디에 해당하는 카테고리가 없습니다."));
        return CategoryResponse.fromEntity(category);
    }

    /** 카테고리 정보 수정하기 */
    @Transactional
    public CategoryResponse updateCategory(Long categoryId, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFound("아이디에 해당하는 카테고리가 없습니다."));
        category.updateCategory(request);
        return CategoryResponse.fromEntity(category);
    }

    /** 카테고리 삭제하기 */
    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFound("아이디에 해당하는 카테고리가 없습니다."));
        categoryRepository.delete(category);    // 엔티티 삭제 (cascade + orphanRemoval 작동)
    }
}
