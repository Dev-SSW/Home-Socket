package Homepage.practice.Category;

import Homepage.practice.Category.DTO.CategoryRequest;
import Homepage.practice.Category.DTO.CategoryResponse;
import Homepage.practice.Category.DTO.CategoryUpdateRequest;
import Homepage.practice.Exception.GlobalApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Category", description = "카테고리 관리 API")
@RestController
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping("/admin/category/createCategory")
    @Operation(summary = "카테고리 생성하기")
    public ResponseEntity<GlobalApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.ok(GlobalApiResponse.success("카테고리 생성 성공", response));
    }

    @GetMapping("/public/category/getAllCategory")
    @Operation(summary = "전체 카테고리 가져오기")
    public ResponseEntity<GlobalApiResponse<List<CategoryResponse>>> getAllCategory() {
        List<CategoryResponse> responses = categoryService.getAllCategory();
        return ResponseEntity.ok(GlobalApiResponse.success("전체 카테고리 가져오기 성공", responses));
    }

    @GetMapping("/public/category/getCategory/{categoryId}")
    @Operation(summary = "특정 카테고리 가져오기")
    public ResponseEntity<GlobalApiResponse<CategoryResponse>> getCategory(@PathVariable(name = "categoryId") Long categoryId) {
        CategoryResponse response = categoryService.getCategory(categoryId);
        return ResponseEntity.ok(GlobalApiResponse.success("카테고리 가져오기 성공", response));
    }

    @PutMapping("/admin/category/updateCategory/{categoryId}")
    @Operation(summary = "카테고리 수정하기")
    public ResponseEntity<GlobalApiResponse<CategoryResponse>> updateCategory(@PathVariable(name = "categoryId") Long categoryId,
                                                                              @Valid @RequestBody CategoryUpdateRequest request) {
        CategoryResponse response = categoryService.updateCategory(categoryId, request);
        return ResponseEntity.ok(GlobalApiResponse.success("카테고리 수정 성공", response));
    }

    @DeleteMapping("/admin/category/deleteCategory/{categoryId}")
    @Operation(summary = "카테고리 삭제하기")
    public ResponseEntity<GlobalApiResponse<?>> deleteCategory(@PathVariable(name = "categoryId") Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok(GlobalApiResponse.success("카테고리 삭제 성공", null));
    }
}
