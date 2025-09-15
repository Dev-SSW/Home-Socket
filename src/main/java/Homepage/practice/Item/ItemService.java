package Homepage.practice.Item;

import Homepage.practice.Category.Category;
import Homepage.practice.Category.CategoryRepository;
import Homepage.practice.Exception.CategoryNotFound;
import Homepage.practice.Exception.ItemNotFound;
import Homepage.practice.Item.DTO.ItemRequest;
import Homepage.practice.Item.DTO.ItemResponse;
import Homepage.practice.Item.DTO.ItemUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;

    /** 상품 생성 */
    @Transactional
    public ItemResponse createItem(Long categoryId, ItemRequest request) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFound("아이디에 해당하는 카테고리가 없습니다."));
        Item item = Item.createItem(category, request);
        Item save = itemRepository.save(item);
        return ItemResponse.fromEntity(save);
    }

    /** 전체 상품 조회 */
    public List<ItemResponse> getAllItem() {
        return itemRepository.findAll().stream()
                .map(ItemResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /** 특정 상품 조회 */
    public ItemResponse getItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFound("아이디에 해당하는 아이템이 없습니다."));
        return ItemResponse.fromEntity(item);
    }

    /** 상품 수정하기 */
    @Transactional
    public ItemResponse updateItem(Long itemId, ItemUpdateRequest request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFound("아이디에 해당하는 아이템이 없습니다."));
        item.updateItem(request);
        return ItemResponse.fromEntity(item);
    }

    /** 상품 삭제하기 */
    @Transactional
    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFound("아이디에 해당하는 아이템이 없습니다."));
        itemRepository.delete(item);
    }
}
