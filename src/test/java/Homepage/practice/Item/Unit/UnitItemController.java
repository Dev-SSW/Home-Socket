package Homepage.practice.Item.Unit;

import Homepage.practice.Category.CategoryRepository;
import Homepage.practice.Item.DTO.ItemRequest;
import Homepage.practice.Item.DTO.ItemResponse;
import Homepage.practice.Item.DTO.ItemUpdateRequest;
import Homepage.practice.Item.ItemController;
import Homepage.practice.Item.ItemRepository;
import Homepage.practice.Item.ItemService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@AutoConfigureMockMvc
public class UnitItemController {
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

    // ItemController에 필요한 MockitoBean, 외부 의존성 가짜 객체
    @MockitoBean
    private ItemService itemService;

    // ItemService에 필요한 MockitoBean, 외부 의존성 가짜 객체
    @MockitoBean
    private ItemRepository itemRepository;
    @MockitoBean
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("상품 생성 성공")
    @WithMockUser(roles = "ADMIN")
    void createItem_success() throws Exception {
        //given
        ItemRequest request = new ItemRequest("item1", 100, 10000);
        ItemResponse response = ItemResponse.builder()
                .id(1L).name("item1").stock(100).itemPrice(10000).avgStar(0).build();
        given(itemService.createItem(anyLong(), any(ItemRequest.class))).willReturn(response);
        // when & then
        mockMvc.perform(post("/admin/category/{categoryId}/item/createItem/", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("상품 생성 성공"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("item1"))
                .andExpect(jsonPath("$.data.stock").value(100))
                .andExpect(jsonPath("$.data.itemPrice").value(10000));
    }

    @Test
    @DisplayName("전체 상품 조회 성공")
    @WithMockUser
    void getAllItem_success() throws Exception {
        //given
        List<ItemResponse> list = List.of(
                ItemResponse.builder()
                        .id(1L).name("item1").stock(100).itemPrice(10000).avgStar(0).build(),
                ItemResponse.builder()
                        .id(2L).name("item2").stock(200).itemPrice(20000).avgStar(0).build()
        );
        given(itemService.getAllItem()).willReturn(list);
        // when & then
        mockMvc.perform(get("/public/item/getAllItem")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("전체 상품 가져오기 성공"))
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("item1"))
                .andExpect(jsonPath("$.data[1].id").value(2L))
                .andExpect(jsonPath("$.data[1].name").value("item2"));
    }

    @Test
    @DisplayName("특정 상품 조회 성공")
    @WithMockUser
    void getItem_success() throws Exception {
        //given
        ItemResponse response = ItemResponse.builder()
                .id(1L).name("item1").stock(100).itemPrice(10000).avgStar(0).build();
        given(itemService.getItem(anyLong())).willReturn(response);
        // when & then
        mockMvc.perform(get("/public/item/getItem/{itemId}", 1L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("상품 가져오기 성공"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("item1"))
                .andExpect(jsonPath("$.data.stock").value(100))
                .andExpect(jsonPath("$.data.itemPrice").value(10000));
    }

    @Test
    @DisplayName("상품 수정하기 성공")
    @WithMockUser(roles = "ADMIN")
    void updateItem_success() throws Exception {
        //given
        ItemUpdateRequest request = new ItemUpdateRequest("item2", 200, 20000);
        ItemResponse response = ItemResponse.builder()
                .id(1L).name("item2").stock(200).itemPrice(20000).avgStar(0).build();
        given(itemService.updateItem(anyLong(), any(ItemUpdateRequest.class))).willReturn(response);
        // when & then
        mockMvc.perform(put("/admin/item/updateItem/{itemId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("상품 수정 성공"))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("item2"))
                .andExpect(jsonPath("$.data.stock").value(200))
                .andExpect(jsonPath("$.data.itemPrice").value(20000));
    }

    @Test
    @DisplayName("상품 삭제하기 성공")
    @WithMockUser(roles = "ADMIN")
    void deleteItem_succes() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/item/deleteItem/{itemId}", 1L)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("상품 삭제 성공"));
    }
}
