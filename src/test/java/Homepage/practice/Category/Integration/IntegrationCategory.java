package Homepage.practice.Category.Integration;

import Homepage.practice.Category.Category;
import Homepage.practice.Category.CategoryRepository;
import Homepage.practice.Category.DTO.CategoryRequest;
import Homepage.practice.Category.DTO.CategoryUpdateRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc  // MockMvc 빈 자동 구성
@Transactional
@Rollback
public class IntegrationCategory {
    // 테스트 인프라
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    // 테스트 시 사용
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("카테고리 생성 성공")
    @WithMockUser(roles = "ADMIN")
    void createCategory_success() throws Exception {
        CategoryRequest categoryRequest = new CategoryRequest("category1", 0, 1, null);

        mockMvc.perform(post("/admin/category/createCategory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("카테고리 생성 성공"))
                //.andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("category1"))
                .andExpect(jsonPath("$.data.depth").value(0))
                .andExpect(jsonPath("$.data.orderIndex").value(1))
                .andExpect(jsonPath("$.data.parentId").doesNotExist())
                .andExpect(jsonPath("$.data.childrenIds").isArray());
    }

    @Test
    @DisplayName("카테고리 생성 실패 - 부모 존재하지 않음")
    @WithMockUser(roles = "ADMIN")
    void createCategory_fail() throws Exception {
        CategoryRequest categoryRequest = new CategoryRequest("category1", 0, 1, 1L);

        mockMvc.perform(post("/admin/category/createCategory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryRequest))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 부모가 없습니다."))
                .andExpect(jsonPath("$.error").value("CATEGORY_PARENT_NOT_FOUND"));
    }

    @Test
    @DisplayName("전체 카테고리 정보 가져오기 성공")
    @WithMockUser
    void getAllCategory_success() throws Exception {
        CategoryRequest categoryRequest1 = new CategoryRequest("category1", 0, 1, null);
        CategoryRequest categoryRequest2 = new CategoryRequest("category2", 0, 2, null);
        Category category1 = Category.createCategory(categoryRequest1, null);
        Category category2 = Category.createCategory(categoryRequest2, null);
        categoryRepository.save(category1);
        categoryRepository.save(category2);

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
        CategoryRequest categoryRequest1 = new CategoryRequest("category1", 0, 1, null);
        Category category1 = Category.createCategory(categoryRequest1, null);
        categoryRepository.save(category1);

        mockMvc.perform(get("/public/category/getCategory/{categoryId}", category1.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("카테고리 가져오기 성공"))
                //.andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("category1"))
                .andExpect(jsonPath("$.data.depth").value(0))
                .andExpect(jsonPath("$.data.orderIndex").value(1))
                .andExpect(jsonPath("$.data.parentId").doesNotExist())
                .andExpect(jsonPath("$.data.childrenIds").isArray());
    }

    @Test
    @DisplayName("특정 카테고리 정보 가져오기 실패 - 해당 카테고리 없음")
    @WithMockUser
    void getCategory_fail() throws Exception {
        mockMvc.perform(get("/public/category/getCategory/{categoryId}", 1L)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 카테고리가 없습니다."))
                .andExpect(jsonPath("$.error").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    @DisplayName("카테고리 정보 수정하기 성공")
    @WithMockUser(roles = "ADMIN")
    void updateCategory_success() throws Exception {
        CategoryRequest categoryRequest1 = new CategoryRequest("category1", 0, 1, null);
        Category category1 = Category.createCategory(categoryRequest1, null);
        categoryRepository.save(category1);
        CategoryUpdateRequest categoryUpdateRequest = new CategoryUpdateRequest("categoryUp", 2);

        // when & then
        mockMvc.perform(put("/admin/category/updateCategory/{categoryId}", category1.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryUpdateRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("카테고리 수정 성공"))
                //.andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("categoryUp"))
                .andExpect(jsonPath("$.data.depth").value(0))
                .andExpect(jsonPath("$.data.orderIndex").value(2))
                .andExpect(jsonPath("$.data.parentId").doesNotExist())
                .andExpect(jsonPath("$.data.childrenIds").isArray());
    }

    @Test
    @DisplayName("카테고리 정보 수정하기 실패 - 해당 카테고리 없음")
    @WithMockUser(roles = "ADMIN")
    void updateCategory_fail() throws Exception {
        CategoryUpdateRequest categoryUpdateRequest = new CategoryUpdateRequest("categoryUp", 2);

        // when & then
        mockMvc.perform(put("/admin/category/updateCategory/{categoryId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryUpdateRequest))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 카테고리가 없습니다."))
                .andExpect(jsonPath("$.error").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    @DisplayName("카테고리 삭제하기 성공")
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_success() throws Exception {
        CategoryRequest categoryRequest1 = new CategoryRequest("category1", 0, 1, null);
        Category category1 = Category.createCategory(categoryRequest1, null);
        categoryRepository.save(category1);
        mockMvc.perform(delete("/admin/category/deleteCategory/{categoryId}", category1.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("카테고리 삭제 성공"));
    }

    @Test
    @DisplayName("카테고리 삭제하기 실패 - 해당 카테고리 없음")
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_fail() throws Exception {
        mockMvc.perform(delete("/admin/category/deleteCategory/{categoryId}", 1L)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 카테고리가 없습니다."))
                .andExpect(jsonPath("$.error").value("CATEGORY_NOT_FOUND"));
    }
}
