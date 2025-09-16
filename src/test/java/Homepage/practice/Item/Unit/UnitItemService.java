package Homepage.practice.Item.Unit;

import Homepage.practice.Category.Category;
import Homepage.practice.Category.CategoryRepository;
import Homepage.practice.Category.DTO.CategoryRequest;
import Homepage.practice.Exception.CategoryNotFound;
import Homepage.practice.Exception.ItemNotFound;
import Homepage.practice.Item.DTO.ItemRequest;
import Homepage.practice.Item.DTO.ItemResponse;
import Homepage.practice.Item.DTO.ItemUpdateRequest;
import Homepage.practice.Item.Item;
import Homepage.practice.Item.ItemRepository;
import Homepage.practice.Item.ItemService;
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
public class UnitItemService {
    // 외부 의존성 객체
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private CategoryRepository categoryRepository;

    // 테스트 할 클래스
    @InjectMocks
    private ItemService itemService;

    @Test
    @DisplayName("상품 생성 성공")
    void createItem_success() {
        // given
        Category category1 = Category.createCategory(new CategoryRequest("category1", 0, 1, null), null);
        ItemRequest request = new ItemRequest("item1", 100, 10000);
        Item item = Item.createItem(category1, request);
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category1));
        given(itemRepository.save(any(Item.class))).willReturn(item);
        // when
        itemService.createItem(1L, request);
        // then
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    @DisplayName("상품 생성 실패 - 해당 카테고리 없음")
    void createItem_fail() {
        // given
        ItemRequest request = new ItemRequest("item1", 100, 10000);
        given(categoryRepository.findById(1L)).willReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> itemService.createItem(1L, request))
                .isInstanceOf(CategoryNotFound.class)
                .hasMessage("아이디에 해당하는 카테고리가 없습니다.");
    }

    @Test
    @DisplayName("전체 상품 조회 성공")
    void getAllItem_success() {
        // given
        Category category1 = Category.createCategory(new CategoryRequest("category1", 0, 1, null), null);
        Item item1 = Item.createItem(category1, new ItemRequest("item1", 100, 10000));
        Item item2 = Item.createItem(category1, new ItemRequest("item2", 200, 20000));
        given(itemRepository.findAll()).willReturn(Arrays.asList(item1, item2));
        // when
        List<ItemResponse> responses = itemService.getAllItem();
        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getName()).isEqualTo("item1");
        assertThat(responses.get(1).getName()).isEqualTo("item2");
    }

    @Test
    @DisplayName("특정 상품 조회 성공")
    void getItem_success() {
        // given
        Category category1 = Category.createCategory(new CategoryRequest("category1", 0, 1, null), null);
        Item item = Item.createItem(category1, new ItemRequest("item1", 100, 10000));
        given(itemRepository.findById(1L)).willReturn(Optional.of(item));
        // when
        ItemResponse response = itemService.getItem(1L);
        // then
        assertThat(response.getName()).isEqualTo("item1");
    }

    @Test
    @DisplayName("특정 상품 조회 실패 - 해당 아이템 없음")
    void getItem_fail() {
        // given
        given(itemRepository.findById(1L)).willReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> itemService.getItem(1L))
                .isInstanceOf(ItemNotFound.class)
                .hasMessage("아이디에 해당하는 아이템이 없습니다.");
    }

    @Test
    @DisplayName("상품 수정하기 성공")
    void updateItem_success() {
        // given
        Category category1 = Category.createCategory(new CategoryRequest("category1", 0, 1, null), null);
        Item item = Item.createItem(category1, new ItemRequest("item1", 100, 10000));
        ItemUpdateRequest request = new ItemUpdateRequest("updateItem", 200, 20000);
        given(itemRepository.findById(1L)).willReturn(Optional.of(item));
        // when
        ItemResponse response = itemService.updateItem(1L, request);
        // then
        assertThat(response.getName()).isEqualTo("updateItem");
        assertThat(response.getStock()).isEqualTo(200);
        assertThat(response.getItemPrice()).isEqualTo(20000);
    }

    @Test
    @DisplayName("상품 수정하기 실패 - 해당 아이템 없음")
    void updateItem_fail() {
        // given
        ItemUpdateRequest request = new ItemUpdateRequest("updateItem", 200, 20000);
        given(itemRepository.findById(1L)).willReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> itemService.updateItem(1L, request))
                .isInstanceOf(ItemNotFound.class)
                .hasMessage("아이디에 해당하는 아이템이 없습니다.");
    }

    @Test
    @DisplayName("상품 삭제하기 성공")
    void deleteItem_succes() {
        // given
        Category category1 = Category.createCategory(new CategoryRequest("category1", 0, 1, null), null);
        Item item = Item.createItem(category1, new ItemRequest("item1", 100, 10000));
        given(itemRepository.findById(1L)).willReturn(Optional.of(item));
        // when
        itemService.deleteItem(1L);
        // then
        verify(itemRepository).delete(any(Item.class));
    }

    @Test
    @DisplayName("상품 삭제하기 실패 - 해당 아이템 없음")
    void deleteItem_fail() {
        // given
        given(itemRepository.findById(1L)).willReturn(Optional.empty());
        // when & then
        assertThatThrownBy(() -> itemService.deleteItem(1L))
                .isInstanceOf(ItemNotFound.class)
                .hasMessage("아이디에 해당하는 아이템이 없습니다.");
    }
}
