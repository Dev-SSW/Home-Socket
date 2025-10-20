package Homepage.practice.Item;

import Homepage.practice.Category.Category;
import Homepage.practice.Category.CategoryRepository;
import Homepage.practice.Item.DTO.ItemRequest;
import Homepage.practice.Item.DTO.ItemUpdateRequest;
import Homepage.practice.TestIntegrationInit;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
public class IntegrationItem {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private ItemRepository itemRepository;
    @Autowired private CategoryRepository categoryRepository;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = TestIntegrationInit.createCategory(categoryRepository);
    }

    @Test
    @Transactional
    @DisplayName("상품 생성 성공")
    @WithMockUser(roles = "ADMIN")
    void createItem_success() throws Exception {
        // given

        // when & then
        mockMvc.perform(post("/admin/category/{categoryId}/item/createItem/", testCategory.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ItemRequest("item1", 1000, 10000)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("상품 생성 성공"))
                .andExpect(jsonPath("$.data.name").value("item1"))
                .andExpect(jsonPath("$.data.stock").value(1000))
                .andExpect(jsonPath("$.data.itemPrice").value(10000));
    }

    @Test
    @Transactional
    @DisplayName("상품 생성 실패 - 해당 카테고리 없음")
    @WithMockUser(roles = "ADMIN")
    void createItem_fail() throws Exception {
        // given

        // when & then
        mockMvc.perform(post("/admin/category/{categoryId}/item/createItem/", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ItemRequest("item1", 1000, 10000)))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 카테고리가 없습니다."))
                .andExpect(jsonPath("$.error").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    @Transactional
    @DisplayName("전체 상품 조회 성공")
    @WithMockUser
    void getAllItem_success() throws Exception {
        // given
        Item testItem = TestIntegrationInit.createItem(itemRepository, testCategory);

        // when & then
        mockMvc.perform(get("/public/item/getAllItem")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("전체 상품 가져오기 성공"))
                .andExpect(jsonPath("$.data[0].name").value("item1"));
    }

    @Test
    @Transactional
    @DisplayName("특정 상품 조회 성공")
    @WithMockUser
    void getItem_success() throws Exception {
        // given
        Item testItem = TestIntegrationInit.createItem(itemRepository, testCategory);

        // when & then
        mockMvc.perform(get("/public/item/getItem/{itemId}", testItem.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("상품 가져오기 성공"))
                .andExpect(jsonPath("$.data.name").value("item1"));
    }

    @Test
    @Transactional
    @DisplayName("특정 상품 조회 실패 - 해당 아이템 없음")
    @WithMockUser
    void getItem_fail() throws Exception {
        // given

        // when & then
        mockMvc.perform(get("/public/item/getItem/{itemId}", 999L)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 아이템이 없습니다."))
                .andExpect(jsonPath("$.error").value("ITEM_NOT_FOUND"));
    }

    @Test
    @Transactional
    @DisplayName("상품 수정하기 성공")
    @WithMockUser(roles = "ADMIN")
    void updateItem_success() throws Exception {
        // given
        Item testItem = TestIntegrationInit.createItem(itemRepository, testCategory);

        // when & then
        mockMvc.perform(put("/admin/item/updateItem/{itemId}", testItem.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ItemUpdateRequest("item2", 2000, 20000)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("상품 수정 성공"))
                .andExpect(jsonPath("$.data.name").value("item2"))
                .andExpect(jsonPath("$.data.stock").value(2000))
                .andExpect(jsonPath("$.data.itemPrice").value(20000));
    }

    @Test
    @Transactional
    @DisplayName("상품 수정하기 실패 - 해당 아이템 없음")
    @WithMockUser(roles = "ADMIN")
    void updateItem_fail() throws Exception {
        // given

        // when & then
        mockMvc.perform(put("/admin/item/updateItem/{itemId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ItemUpdateRequest("item2", 200, 20000)))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 아이템이 없습니다."))
                .andExpect(jsonPath("$.error").value("ITEM_NOT_FOUND"));
    }

    @Test
    @Transactional
    @DisplayName("상품 삭제하기 성공")
    @WithMockUser(roles = "ADMIN")
    void deleteItem_succes() throws Exception {
        // given
        Item testItem = TestIntegrationInit.createItem(itemRepository, testCategory);

        // when & then
        mockMvc.perform(delete("/admin/item/deleteItem/{itemId}", testItem.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("상품 삭제 성공"));
    }

    @Test
    @Transactional
    @DisplayName("상품 삭제하기 실패 - 해당 아이템 없음")
    @WithMockUser(roles = "ADMIN")
    void deleteItem_fail() throws Exception {
        // given

        // when & then
        mockMvc.perform(delete("/admin/item/deleteItem/{itemId}", 999L)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 아이템이 없습니다."))
                .andExpect(jsonPath("$.error").value("ITEM_NOT_FOUND"));
    }
}
