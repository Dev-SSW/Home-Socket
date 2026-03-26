package Homepage.practice.Category;

import Homepage.practice.Category.DTO.CategoryRequest;
import Homepage.practice.Category.DTO.CategoryUpdateRequest;
import Homepage.practice.TestIntegrationInit;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
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
@AutoConfigureMockMvc
public class IntegrationCategory {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private CategoryRepository categoryRepository;

    @Test
    @Transactional
    @DisplayName("카테고리 생성 성공")
    @WithMockUser(roles = "ADMIN")
    void createCategory_success() throws Exception {
        // given
        
        // when & then
        mockMvc.perform(post("/admin/category/createCategory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequest("category1", 0, 1, null)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("카테고리 생성 성공"))
                .andExpect(jsonPath("$.data.name").value("category1"))
                .andExpect(jsonPath("$.data.depth").value(0))
                .andExpect(jsonPath("$.data.orderIndex").value(1))
                .andExpect(jsonPath("$.data.parentId").doesNotExist())
                .andExpect(jsonPath("$.data.childrenIds").isArray());
    }

    @Test
    @Transactional
    @DisplayName("카테고리 생성 실패 - 부모 존재하지 않음")
    @WithMockUser(roles = "ADMIN")
    void createCategory_fail() throws Exception {
        // given

        // when & then
        mockMvc.perform(post("/admin/category/createCategory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryRequest("category1", 0, 1, 999L)))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 부모가 없습니다."))
                .andExpect(jsonPath("$.error").value("CATEGORY_PARENT_NOT_FOUND"));
    }

    @Test
    @Transactional
    @DisplayName("루트 카테고리 가져오기 성공")
    @WithMockUser
    void getRootCategory_success() throws Exception {
        // given
        Category testCategory = TestIntegrationInit.createCategory(categoryRepository);
        
        // when & then
        mockMvc.perform(get("/public/category/getRootCategory")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("루트 카테고리 가져오기 성공"))
                .andExpect(jsonPath("$.data[0].name").value("category1"))
                .andExpect(jsonPath("$.data[0].depth").value(0))
                .andExpect(jsonPath("$.data[0].children").isArray());
    }

    @Test
    @Transactional
    @DisplayName("자식 카테고리 가져오기 성공")
    @WithMockUser
    void getChildCategory_success() throws Exception {
        // given
        Category parentCategory = TestIntegrationInit.createCategory(categoryRepository);
        
        // 자식 카테고리 직접 생성
        CategoryRequest childRequest = new CategoryRequest("childCategory", 1, 1, parentCategory.getId());
        Category category = Category.createCategory(childRequest, parentCategory);
        categoryRepository.save(category);

        // when & then
        mockMvc.perform(get("/public/category/getChildrenCategory/{parentId}", parentCategory.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("자식 카테고리 가져오기 성공"))
                .andExpect(jsonPath("$.data[0].name").value("childCategory"))
                .andExpect(jsonPath("$.data[0].depth").value(1))
                .andExpect(jsonPath("$.data[0].children").isArray());
    }

    @Test
    @Transactional
    @DisplayName("자식 카테고리 가져오기 실패 - 부모 카테고리 없음")
    @WithMockUser
    void getChildCategory_fail() throws Exception {
        // given

        // when & then
        mockMvc.perform(get("/public/category/getChildrenCategory/{parentId}", 999L)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 부모 카테고리가 없습니다."))
                .andExpect(jsonPath("$.error").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    @Transactional
    @DisplayName("루트 카테고리 가져오기 실패 - 루트 카테고리 없음")
    @WithMockUser
    void getRootCategory_fail() throws Exception {
        // given - 빈 데이터베이스 상태

        // when & then
        mockMvc.perform(get("/public/category/getRootCategory")
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("루트 카테고리가 존재하지 않습니다."))
                .andExpect(jsonPath("$.error").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    @Transactional
    @DisplayName("카테고리 정보 수정하기 성공")
    @WithMockUser(roles = "ADMIN")
    void updateCategory_success() throws Exception {
        // given
        Category testCategory = TestIntegrationInit.createCategory(categoryRepository);

        // when & then
        mockMvc.perform(put("/admin/category/updateCategory/{categoryId}", testCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryUpdateRequest("updateCategory", 2)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("카테고리 수정 성공"))
                .andExpect(jsonPath("$.data.name").value("updateCategory"))
                .andExpect(jsonPath("$.data.depth").value(0))
                .andExpect(jsonPath("$.data.orderIndex").value(2))
                .andExpect(jsonPath("$.data.parentId").doesNotExist())
                .andExpect(jsonPath("$.data.childrenIds").isArray());
    }

    @Test
    @Transactional
    @DisplayName("카테고리 정보 수정하기 실패 - 해당 카테고리 없음")
    @WithMockUser(roles = "ADMIN")
    void updateCategory_fail() throws Exception {
        // given

        // when & then
        mockMvc.perform(put("/admin/category/updateCategory/{categoryId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CategoryUpdateRequest("updateCategory", 2)))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 카테고리가 없습니다."))
                .andExpect(jsonPath("$.error").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    @Transactional
    @DisplayName("카테고리 삭제하기 성공")
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_success() throws Exception {
        // given
        Category testCategory = TestIntegrationInit.createCategory(categoryRepository);

        // when & then
        mockMvc.perform(delete("/admin/category/deleteCategory/{categoryId}", testCategory.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("카테고리 삭제 성공"));
    }

    @Test
    @Transactional
    @DisplayName("카테고리 삭제하기 실패 - 해당 카테고리 없음")
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_fail() throws Exception {
        // given

        // when & then
        mockMvc.perform(delete("/admin/category/deleteCategory/{categoryId}", 1L)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 카테고리가 없습니다."))
                .andExpect(jsonPath("$.error").value("CATEGORY_NOT_FOUND"));
    }
}
