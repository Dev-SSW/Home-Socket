package Homepage.practice.Category;

import Homepage.practice.Category.DTO.CategoryRequest;
import Homepage.practice.Category.DTO.CategoryResponse;
import Homepage.practice.Category.DTO.CategoryUpdateRequest;
import Homepage.practice.TestUnitInit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UnitCategory {
    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private CategoryService categoryService;

    @Test
    @DisplayName("카테고리 생성 성공")
    void createCategory_success() {
        // given
        Category testCategory = TestUnitInit.createCategory(1L);

        // when
        CategoryResponse response = categoryService.createCategory(new CategoryRequest("category1", 0, 1, null));

        // then
        verify(categoryRepository).save(any(Category.class));
        assertThat(testCategory.getName()).isEqualTo("category1");
    }

    @Test
    @DisplayName("카테고리 정보 수정하기 성공")
    void updateCategory_success() {
        // given
        Category testCategory = TestUnitInit.createCategory(1L);
        given(categoryRepository.findById(testCategory.getId())).willReturn(Optional.of(testCategory));

        // when
        CategoryResponse response = categoryService.updateCategory(
                testCategory.getId(), new CategoryUpdateRequest("updateCategory", 2));

        // then
        assertThat(testCategory.getName()).isEqualTo("updateCategory");
        assertThat(testCategory.getOrderIndex()).isEqualTo(2);
    }

    @Test
    @DisplayName("카테고리 삭제하기 성공")
    void deleteCategory_success() {
        // given
        Category testCategory = TestUnitInit.createCategory(1L);
        given(categoryRepository.findById(testCategory.getId())).willReturn(Optional.of(testCategory));

        // when
        categoryService.deleteCategory(testCategory.getId());

        // then
        verify(categoryRepository).delete(any(Category.class));
    }
}
