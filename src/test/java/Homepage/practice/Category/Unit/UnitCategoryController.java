package Homepage.practice.Category.Unit;

import Homepage.practice.Category.CategoryController;
import Homepage.practice.Category.CategoryRepository;
import Homepage.practice.Category.CategoryService;
import Homepage.practice.Category.DTO.CategoryRequest;
import Homepage.practice.Category.DTO.CategoryResponse;
import Homepage.practice.Category.DTO.CategoryUpdateRequest;
import Homepage.practice.User.JWT.JwtUtils;
import Homepage.practice.User.UserRepository;
import Homepage.practice.User.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc
public class UnitCategoryController {
    // 테스트 인프라
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    // 유저 권한 별 api 확인
    @MockitoBean
    private JwtUtils jwtUtils;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private UserRepository userRepository;

    // CategoryController에 필요한 MockitoBean, 외부 의존성 가짜 객체
    @MockitoBean
    private CategoryService categoryService;

    // CategoryService에 필요한 MockitoBean, 외부 의존성 가짜 객체
    @MockitoBean
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("카테고리 생성 성공")
    @WithMockUser(roles = "ADMIN")
    void createCategory_success() throws Exception {
        // given
        CategoryRequest categoryRequest = new CategoryRequest("category1", 0, 1, null);
        CategoryResponse categoryResponse = CategoryResponse.builder()
                .id(1L).name("category1").depth(0).orderIndex(1).parentId(null).childrenIds(List.of()).build();
        given(categoryService.createCategory(any(CategoryRequest.class))).willReturn(categoryResponse);

        mockMvc.perform(post("/admin/category/createCategory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("카테고리 생성 성공"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("category1"))
                .andExpect(jsonPath("$.data.depth").value(0))
                .andExpect(jsonPath("$.data.orderIndex").value(1))
                .andExpect(jsonPath("$.data.parentId").doesNotExist())
                .andExpect(jsonPath("$.data.childrenIds").isArray());
    }

    @Test
    @DisplayName("전체 카테고리 정보 가져오기 성공")
    @WithMockUser
    void getAllCategory_success() throws Exception {
        List<CategoryResponse> list = List.of(
                CategoryResponse.builder()
                        .id(1L).name("category1").depth(0).orderIndex(1).parentId(null).childrenIds(List.of()).build(),
                CategoryResponse.builder()
                        .id(2L).name("category2").depth(0).orderIndex(2).parentId(null).childrenIds(List.of()).build()
        );
        given(categoryService.getAllCategory()).willReturn(list);

        mockMvc.perform(get("/public/category/getAllCategory")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("전체 카테고리 가져오기 성공"))
                .andExpect(jsonPath("$.data[0].name").value("category1"))
                .andExpect(jsonPath("$.data[1].name").value("category2"));
    }

    @Test
    @DisplayName("특정 카테고리 정보 가져오기 성공")
    @WithMockUser
    void getCategory_success() throws Exception {
        // given
        CategoryResponse categoryResponse = CategoryResponse.builder()
                .id(1L).name("category1").depth(0).orderIndex(1).parentId(null).childrenIds(List.of()).build();
        given(categoryService.getCategory(1L)).willReturn(categoryResponse);

        // when & then
        mockMvc.perform(get("/public/category/getCategory/{categoryId}", 1L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("카테고리 가져오기 성공"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("category1"))
                .andExpect(jsonPath("$.data.depth").value(0))
                .andExpect(jsonPath("$.data.orderIndex").value(1))
                .andExpect(jsonPath("$.data.parentId").doesNotExist())
                .andExpect(jsonPath("$.data.childrenIds").isArray());
    }

    @Test
    @DisplayName("카테고리 정보 수정하기 성공")
    @WithMockUser(roles = "ADMIN")
    void updateCategory_success() throws Exception {
        // given
        CategoryResponse categoryResponse = CategoryResponse.builder()
                .id(1L).name("categoryUp").depth(0).orderIndex(2).parentId(null).childrenIds(List.of()).build();
        CategoryUpdateRequest categoryUpdateRequest = new CategoryUpdateRequest("categoryUp", 2);
        given(categoryService.updateCategory(eq(1L), any(CategoryUpdateRequest.class))).willReturn(categoryResponse);

        // when & then
        mockMvc.perform(put("/admin/category/updateCategory/{categoryId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryUpdateRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("카테고리 수정 성공"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("categoryUp"))
                .andExpect(jsonPath("$.data.depth").value(0))
                .andExpect(jsonPath("$.data.orderIndex").value(2))
                .andExpect(jsonPath("$.data.parentId").doesNotExist())
                .andExpect(jsonPath("$.data.childrenIds").isArray());
    }

    @Test
    @DisplayName("카테고리 삭제하기 성공")
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_success() throws Exception {
        mockMvc.perform(delete("/admin/category/deleteCategory/{categoryId}", 1L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("카테고리 삭제 성공"));
    }
}
