package Homepage.practice.Item;

import Homepage.practice.Category.Category;
import Homepage.practice.Category.CategoryRepository;
import Homepage.practice.Exception.CategoryNotFound;
import Homepage.practice.Exception.ItemNotFound;
import Homepage.practice.Item.DTO.ItemRequest;
import Homepage.practice.Item.DTO.ItemResponse;
import Homepage.practice.Item.DTO.ItemResponseCategory;
import Homepage.practice.Item.DTO.ItemUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        itemRepository.save(item);
        return ItemResponse.fromEntity(item);
    }

    /** 전체 상품 조회 */
    public Page<ItemResponse> getAllItem(Pageable pageable) {
        return itemRepository.findAllItem(pageable);
    }

    /** 특정 상품 조회 */
    public ItemResponse getItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFound("아이디에 해당하는 아이템이 없습니다."));
        return ItemResponse.fromEntity(item);
    }

    /** 카테고리 별 아이템 조회 */
    public Page<ItemResponseCategory> getItemsByCategory(Long categoryId, Pageable pageable) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new CategoryNotFound("아이디에 해당하는 카테고리가 없습니다.");
        }
        return itemRepository.findItemsByCategory(categoryId, pageable);
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
