package Homepage.practice.Item.Integration;

import Homepage.practice.Category.Category;
import Homepage.practice.Category.CategoryRepository;
import Homepage.practice.Category.DTO.CategoryRequest;
import Homepage.practice.Item.DTO.ItemRequest;
import Homepage.practice.Item.DTO.ItemUpdateRequest;
import Homepage.practice.Item.Item;
import Homepage.practice.Item.ItemRepository;
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
public class IntegrationItem {
    // 테스트 인프라
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    // 테스트 시 사용
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("상품 생성 성공")
    @WithMockUser(roles = "ADMIN")
    void createItem_success() throws Exception {
        //given
        CategoryRequest categoryRequest = new CategoryRequest("category1", 0, 1, null);
        Category category = Category.createCategory(categoryRequest, null);
        categoryRepository.save(category);
        ItemRequest request = new ItemRequest("item1", 100, 10000);

        // when & then
        mockMvc.perform(post("/admin/category/{categoryId}/item/createItem/", category.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("상품 생성 성공"))
                //.andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("item1"))
                .andExpect(jsonPath("$.data.stock").value(100))
                .andExpect(jsonPath("$.data.itemPrice").value(10000));
    }

    @Test
    @DisplayName("상품 생성 실패 - 해당 카테고리 없음")
    @WithMockUser(roles = "ADMIN")
    void createItem_fail() throws Exception {
        //given
        ItemRequest request = new ItemRequest("item1", 100, 10000);

        // when & then
        mockMvc.perform(post("/admin/category/{categoryId}/item/createItem/", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 카테고리가 없습니다."))
                .andExpect(jsonPath("$.error").value("CATEGORY_NOT_FOUND"));
    }

    @Test
    @DisplayName("전체 상품 조회 성공")
    @WithMockUser
    void getAllItem_success() throws Exception {
        //given
        CategoryRequest categoryRequest = new CategoryRequest("category1", 0, 1, null);
        Category category = Category.createCategory(categoryRequest, null);
        categoryRepository.save(category);
        ItemRequest request1 = new ItemRequest("item1", 100, 10000);
        ItemRequest request2 = new ItemRequest("item2", 200, 20000);
        Item item1 = Item.createItem(category, request1);
        Item item2 = Item.createItem(category, request2);
        itemRepository.save(item1);
        itemRepository.save(item2);

        // when & then
        mockMvc.perform(get("/public/item/getAllItem")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("전체 상품 가져오기 성공"))
                //.andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("item1"))
                //.andExpect(jsonPath("$.data[1].id").value(2L))
                .andExpect(jsonPath("$.data[1].name").value("item2"));
    }

    @Test
    @DisplayName("특정 상품 조회 성공")
    @WithMockUser
    void getItem_success() throws Exception {
        //given
        CategoryRequest categoryRequest = new CategoryRequest("category1", 0, 1, null);
        Category category = Category.createCategory(categoryRequest, null);
        categoryRepository.save(category);
        ItemRequest request = new ItemRequest("item1", 100, 10000);
        Item item = Item.createItem(category, request);
        itemRepository.save(item);

        // when & then
        mockMvc.perform(get("/public/item/getItem/{itemId}", item.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("상품 가져오기 성공"))
                //.andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("item1"))
                .andExpect(jsonPath("$.data.stock").value(100))
                .andExpect(jsonPath("$.data.itemPrice").value(10000));
    }

    @Test
    @DisplayName("특정 상품 조회 실패 - 해당 아이템 없음")
    @WithMockUser
    void getItem_fail() throws Exception {
        // when & then
        mockMvc.perform(get("/public/item/getItem/{itemId}", 1L)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 아이템이 없습니다."))
                .andExpect(jsonPath("$.error").value("ITEM_NOT_FOUND"));
    }

    @Test
    @DisplayName("상품 수정하기 성공")
    @WithMockUser(roles = "ADMIN")
    void updateItem_success() throws Exception {
        //given
        CategoryRequest categoryRequest = new CategoryRequest("category1", 0, 1, null);
        Category category = Category.createCategory(categoryRequest, null);
        categoryRepository.save(category);
        ItemRequest ItemRequest = new ItemRequest("item1", 100, 10000);
        Item item = Item.createItem(category, ItemRequest);
        itemRepository.save(item);
        ItemUpdateRequest request = new ItemUpdateRequest("item2", 200, 20000);

        // when & then
        mockMvc.perform(put("/admin/item/updateItem/{itemId}", item.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("상품 수정 성공"))
                //.andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("item2"))
                .andExpect(jsonPath("$.data.stock").value(200))
                .andExpect(jsonPath("$.data.itemPrice").value(20000));
    }

    @Test
    @DisplayName("상품 수정하기 실패 - 해당 아이템 없음")
    @WithMockUser(roles = "ADMIN")
    void updateItem_fail() throws Exception {
        //given
        ItemUpdateRequest request = new ItemUpdateRequest("item2", 200, 20000);

        // when & then
        mockMvc.perform(put("/admin/item/updateItem/{itemId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 아이템이 없습니다."))
                .andExpect(jsonPath("$.error").value("ITEM_NOT_FOUND"));
    }

    @Test
    @DisplayName("상품 삭제하기 성공")
    @WithMockUser(roles = "ADMIN")
    void deleteItem_succes() throws Exception {
        CategoryRequest categoryRequest = new CategoryRequest("category1", 0, 1, null);
        Category category = Category.createCategory(categoryRequest, null);
        categoryRepository.save(category);
        ItemRequest request = new ItemRequest("item1", 100, 10000);
        Item item = Item.createItem(category, request);
        itemRepository.save(item);

        // when & then
        mockMvc.perform(delete("/admin/item/deleteItem/{itemId}", item.getId())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("상품 삭제 성공"));
    }

    @Test
    @DisplayName("상품 삭제하기 실패 - 해당 아이템 없음")
    @WithMockUser(roles = "ADMIN")
    void deleteItem_fail() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/item/deleteItem/{itemId}", 1L)
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 아이템이 없습니다."))
                .andExpect(jsonPath("$.error").value("ITEM_NOT_FOUND"));
    }
}
