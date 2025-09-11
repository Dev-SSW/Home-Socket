package Homepage.practice.Category.Unit;

import Homepage.practice.Category.Category;
import Homepage.practice.Category.CategoryRepository;
import Homepage.practice.Category.CategoryService;
import Homepage.practice.Category.DTO.CategoryRequest;
import Homepage.practice.Category.DTO.CategoryResponse;
import Homepage.practice.Category.DTO.CategoryUpdateRequest;
import Homepage.practice.Exception.CategoryNotFound;
import Homepage.practice.Exception.CategoryParentNotFound;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class UnitCategoryService {
    // 외부 의존성 객체
    @Mock
    private CategoryRepository categoryRepository;
    // 테스트 할 클래스
    @InjectMocks
    private CategoryService categoryService;

    @Test
    @DisplayName("카테고리 생성 성공")
    void createCategory_success() {
        // given
        CategoryRequest categoryRequest = new CategoryRequest("category1", 0, 1, null);
        Category category1 = Category.createCategory(categoryRequest, null);
        given(categoryRepository.save(any(Category.class))).willReturn(category1);
        // when
        categoryService.createCategory(categoryRequest);
        // then
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("카테고리 생성 실패 - 부모 존재하지 않음")
    void createCategory_fail() {
        // given
        CategoryRequest categoryRequest = new CategoryRequest("category2", 1, 1, 1L);
        given(categoryRepository.findById(1L)).willReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> categoryService.createCategory(categoryRequest))
                .isInstanceOf(CategoryParentNotFound.class)
                .hasMessage("아이디에 해당하는 부모가 없습니다.");
    }

    @Test
    @DisplayName("전체 카테고리 정보 가져오기 성공")
    void getAllCategory_success() {
        // given
        Category category1 = Category.createCategory(new CategoryRequest("category1", 0, 1, null), null);
        Category category2 = Category.createCategory(new CategoryRequest("category2", 0, 2, null), null);
        given(categoryRepository.findAll()).willReturn(Arrays.asList(category1, category2));
        // when
        List<CategoryResponse> responses = categoryService.getAllCategory();
        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("category1");
        assertThat(responses.get(1).getName()).isEqualTo("category2");
    }

    @Test
    @DisplayName("특정 카테고리 정보 가져오기 성공")
    void getCategory_success() {
        // given
        Category category1 = Category.createCategory(new CategoryRequest("category1", 0, 1, null), null);
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category1));
        // when
        CategoryResponse response = categoryService.getCategory(1L);
        // then
        assertThat(response.getName()).isEqualTo("category1");
    }

    @Test
    @DisplayName("특정 카테고리 정보 가져오기 실패 - 해당 카테고리 없음")
    void getCategory_fail() {
        // given
        given(categoryRepository.findById(1L)).willReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> categoryService.getCategory(1L))
                .isInstanceOf(CategoryNotFound.class)
                .hasMessage("아이디에 해당하는 카테고리가 없습니다.");
    }

    @Test
    @DisplayName("카테고리 정보 수정하기 성공")
    void updateCategory_success() {
        // given
        Category category1 = Category.createCategory(new CategoryRequest("category1", 0, 1, null), null);
        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest("categoryUp", 2);
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category1));
        // when
        CategoryResponse response = categoryService.updateCategory(1L, updateRequest);
        // then
        assertThat(category1.getName()).isEqualTo("categoryUp");
        assertThat(category1.getOrderIndex()).isEqualTo(2);
    }

    @Test
    @DisplayName("카테고리 정보 수정하기 실패 - 해당 카테고리 없음")
    void updateCategory_fail() {
        // given
        CategoryUpdateRequest updateRequest = new CategoryUpdateRequest("categoryUp", 2);
        given(categoryRepository.findById(1L)).willReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> categoryService.updateCategory(1L, updateRequest))
                .isInstanceOf(CategoryNotFound.class)
                .hasMessage("아이디에 해당하는 카테고리가 없습니다.");
    }

    @Test
    @DisplayName("카테고리 삭제하기 성공")
    void deleteCategory_success() {
        // given
        Category category1 = Category.createCategory(new CategoryRequest("category1", 0, 1, null), null);
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category1));
        // when
        categoryService.deleteCategory(1L);
        // then
        verify(categoryRepository).delete(any(Category.class));
    }

    @Test
    @DisplayName("카테고리 삭제하기 실패 - 해당 카테고리 없음")
    void deleteCategory_fail() {
        // given
        given(categoryRepository.findById(1L)).willReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                .isInstanceOf(CategoryNotFound.class)
                .hasMessage("아이디에 해당하는 카테고리가 없습니다.");
    }
}
