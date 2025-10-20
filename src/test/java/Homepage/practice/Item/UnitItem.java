package Homepage.practice.Item;

import Homepage.practice.Category.Category;
import Homepage.practice.Category.CategoryRepository;
import Homepage.practice.Item.DTO.ItemRequest;
import Homepage.practice.Item.DTO.ItemResponse;
import Homepage.practice.Item.DTO.ItemUpdateRequest;
import Homepage.practice.TestUnitInit;
import org.junit.jupiter.api.BeforeEach;
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
public class UnitItem{
    @Mock private ItemRepository itemRepository;
    @Mock private CategoryRepository categoryRepository;
    @InjectMocks private ItemService itemService;

    private Category testCategory;

    @BeforeEach
    void setup() {
        testCategory = TestUnitInit.createCategory(1L);
    }

    @Test
    @DisplayName("상품 생성 성공")
    void createItem_success() {
        // given
        given(categoryRepository.findById(testCategory.getId())).willReturn(Optional.of(testCategory));

        // when
        itemService.createItem(testCategory.getId(), new ItemRequest("item1", 100, 10000));

        // then
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    @DisplayName("상품 수정하기 성공")
    void updateItem_success() {
        // given
        Item testItem = TestUnitInit.createItem(2L, testCategory);
        given(itemRepository.findById(testItem.getId())).willReturn(Optional.of(testItem));

        // when
        ItemResponse response = itemService.updateItem(testItem.getId(), new ItemUpdateRequest("item2", 2000, 20000));

        // then
        assertThat(response.getName()).isEqualTo("item2");
        assertThat(response.getStock()).isEqualTo(2000);
        assertThat(response.getItemPrice()).isEqualTo(20000);
    }

    @Test
    @DisplayName("상품 삭제하기 성공")
    void deleteItem_succes() {
        // given
        Item testItem = TestUnitInit.createItem(2L, testCategory);
        given(itemRepository.findById(testItem.getId())).willReturn(Optional.of(testItem));

        // when
        itemService.deleteItem(testItem.getId());

        // then
        verify(itemRepository).delete(any(Item.class));
    }
}
