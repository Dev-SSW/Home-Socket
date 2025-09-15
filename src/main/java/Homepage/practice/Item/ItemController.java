package Homepage.practice.Item;

import Homepage.practice.Exception.GlobalApiResponse;
import Homepage.practice.Item.DTO.ItemRequest;
import Homepage.practice.Item.DTO.ItemResponse;
import Homepage.practice.Item.DTO.ItemUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Item", description = "상품 관리 API")
@RestController
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping("/admin/category/{categoryId}/item/createItem/")
    @Operation(summary = "상품 생성하기")
    public ResponseEntity<GlobalApiResponse<ItemResponse>> createItem(@PathVariable(name = "categoryId") Long categoryId,
                                                                      @Valid @RequestBody ItemRequest request) {
        ItemResponse response = itemService.createItem(categoryId, request);
        return ResponseEntity.ok(GlobalApiResponse.success("상품 생성 성공", response));
    }

    @GetMapping("/public/item/getAllItem")
    @Operation(summary = "전체 상품 가져오기")
    public ResponseEntity<GlobalApiResponse<List<ItemResponse>>> getAllItem() {
        List<ItemResponse> responses = itemService.getAllItem();
        return ResponseEntity.ok(GlobalApiResponse.success("전체 상품 가져오기 성공", responses));
    }

    @GetMapping("/public/item/getItem/{itemId}")
    @Operation(summary = "특정 상품 가져오기")
    public ResponseEntity<GlobalApiResponse<ItemResponse>> getItem(@PathVariable(name = "itemId") Long itemId) {
        ItemResponse response = itemService.getItem(itemId);
        return ResponseEntity.ok(GlobalApiResponse.success("상품 가져오기 성공", response));
    }

    @PutMapping("/admin/item/updateItem/{itemId}")
    @Operation(summary = "상품 수정하기")
    public ResponseEntity<GlobalApiResponse<ItemResponse>> updateItem(@PathVariable(name = "itemId") Long itemId,
                                                                      @Valid @RequestBody ItemUpdateRequest request) {
        ItemResponse response = itemService.updateItem(itemId, request);
        return ResponseEntity.ok(GlobalApiResponse.success("상품 수정 성공", response));
    }

    @DeleteMapping("/admin/item/deleteItem/{itemId}")
    @Operation(summary = "상품 삭제하기")
    public ResponseEntity<GlobalApiResponse<?>> deleteItem(@PathVariable(name = "itemId") Long itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.ok(GlobalApiResponse.success("상품 삭제 성공", null));
    }
}
