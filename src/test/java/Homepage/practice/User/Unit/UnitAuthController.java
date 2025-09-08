package Homepage.practice.User.Unit;

import Homepage.practice.Exception.JwtInvalid;
import Homepage.practice.Exception.UserNotFound;
import Homepage.practice.Exception.UsernameAlreadyExists;
import Homepage.practice.User.*;
import Homepage.practice.User.DTO.*;
import Homepage.practice.User.JWT.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc
public class UnitAuthController {
    // 테스트 인프라
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    // AuthController에 필요한 MockitoBean, 외부 의존성 가짜 객체
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private AuthService authService;

    // AuthService에 필요한 MockitoBean, 외부 의존성 가짜 객체
    @MockitoBean
    private UserRepository userRepository;
    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private JwtUtils jwtUtils;
    @MockitoBean
    private PasswordEncoder passwordEncoder;

    // 테스트 필드
    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private JwtRequest jwtRequest;
    private JwtResponse jwtResponse;
    private User testUser;
    private UserUpdateRequest userUpdateRequest;
    private UserPassUpdateRequest userPassUpdateRequest;

    @BeforeEach
    void setup() {
        signupRequest = new SignupRequest("user1", "pass1", LocalDate.of(2000,1,1), "홍길동");
        loginRequest = new LoginRequest("user1", "pass1");
        jwtRequest = new JwtRequest("token1");
        jwtResponse = JwtResponse.builder()
                .token("accessToken")
                .refreshToken("refreshToken")
                .expirationTime("24Hr")
                .build();
        userUpdateRequest = new UserUpdateRequest(LocalDate.of(1000,10,10), "송길똥");
        userPassUpdateRequest = new UserPassUpdateRequest("pass1", "newPass");
        testUser = User.builder()
                .id(1L)
                .username("user1")
                .password("pass1")
                .birth(LocalDate.of(2000,1,1))
                .name("홍길동")
                .role(Role.ROLE_USER)
                .tokenVersion(1)
                .build();
    }

    @Test
    @DisplayName("회원 가입 성공")
    @WithMockUser
    void signup_success() throws Exception {
        mockMvc.perform(post("/public/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원가입 성공"));
    }

    @Test
    @DisplayName("회원 가입 실패 - 아이디 중복")
    @WithMockUser
    void signup_fail() throws Exception {
        // void 메서드에 예외 던지기
        doThrow(new UsernameAlreadyExists("이미 존재하는 아이디 입니다.")).when(authService).signup(any(SignupRequest.class));

        mockMvc.perform(post("/public/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 존재하는 아이디 입니다."))
                .andExpect(jsonPath("$.error").value("USERNAME_EXISTS"));
    }

    @Test
    @DisplayName("로그인 성공")
    @WithMockUser
    void login_success() throws Exception {
        given(authService.login(any(LoginRequest.class))).willReturn(jwtResponse);

        mockMvc.perform(post("/public/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.data.token").value("accessToken"))
                .andExpect(jsonPath("$.data.refreshToken").value("refreshToken"))
                .andExpect(jsonPath("$.data.expirationTime").value("24Hr"));
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 유저")
    @WithMockUser
    void login_fail() throws Exception {
        given(authService.login(any(LoginRequest.class))).willThrow(new UserNotFound("아이디에 해당하는 회원이 없습니다."));

        mockMvc.perform(post("/public/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 회원이 없습니다."))
                .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"));
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    @WithMockUser
    void tokenRenew_success() throws Exception {
        given(authService.tokenRenew(any(JwtRequest.class))).willReturn(jwtResponse);

        mockMvc.perform(post("/public/tokenRenew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jwtRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("재발급 성공"))
                .andExpect(jsonPath("$.data.token").value("accessToken"))
                .andExpect(jsonPath("$.data.refreshToken").value("refreshToken"))
                .andExpect(jsonPath("$.data.expirationTime").value("24Hr"));
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 리프레시 토큰 만료")
    @WithMockUser
    void tokenRenew_fail() throws Exception {
        given(authService.tokenRenew(any(JwtRequest.class))).willThrow(new JwtInvalid("리프레시 토큰이 만료되었습니다."));

        mockMvc.perform(post("/public/tokenRenew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jwtRequest))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("리프레시 토큰이 만료되었습니다."))
                .andExpect(jsonPath("$.error").value("JWT_INVALID"));
    }

    @Test
    @DisplayName("토큰 유효성 검사 성공")
    @WithMockUser
    void validateTest_success() throws Exception {
        mockMvc.perform(post("/public/validateTest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jwtRequest))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("유효한 토큰입니다."));
    }

    @Test
    @DisplayName("토큰 유효성 검사 실패 - 토큰 만료")
    @WithMockUser
    void validateTest_fail() throws Exception {
        doThrow(new JwtInvalid("토큰이 만료되었습니다.")).when(authService).validateTest(any(JwtRequest.class));

        mockMvc.perform(post("/public/validateTest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jwtRequest))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("토큰이 만료되었습니다."))
                .andExpect(jsonPath("$.error").value("JWT_INVALID"));
    }

    @Test
    @DisplayName("전체 유저 정보 가져오기 성공")
    @WithMockUser(roles = "ADMIN")
    void getAllUser() throws Exception {
        List<UserResponse> list = List.of(
                UserResponse.builder()
                        .id(1L).username("user1").name("홍길동").role(Role.ROLE_USER).build(),
                UserResponse.builder()
                        .id(2L).username("user2").name("홍길동2").role(Role.ROLE_USER).build()
        );
        given(userService.getAllUser()).willReturn(list);

        mockMvc.perform(get("/admin/getAllUser")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("전체 정보 조회 성공"))
                .andExpect(jsonPath("$.data[0].username").value("user1"))
                .andExpect(jsonPath("$.data[1].username").value("user2"));
    }

    @Test
    @DisplayName("특정 유저 정보 가져오기 성공")
    void getUser() throws Exception {
        UserResponse resp = UserResponse.builder()
                .id(1L).username("user1").name("홍길동").role(Role.ROLE_USER).build();
        given(userService.getUser("user1")).willReturn(resp);

        mockMvc.perform(get("/user/getUser")
                        .with(user(testUser))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("정보 조회 성공"))
                .andExpect(jsonPath("$.data.username").value("user1"));
    }

    @Test
    @DisplayName("유저 정보 수정하기 성공 (비밀번호 제외)")
    void updateUser() throws Exception {
        mockMvc.perform(put("/user/updateUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRequest))
                        .with(user(testUser))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("정보 수정 성공"));
    }

    @Test
    @DisplayName("비밀번호 수정하기 성공")
    void updatePassword_success() throws Exception {
        mockMvc.perform(put("/user/updatePassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userPassUpdateRequest))
                        .with(user(testUser))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("비밀번호 수정 성공"));
    }

    @Test
    @DisplayName("비밀번호 수정하기 실패 - 비밀번호 오류")
    void updatePassword() throws Exception {
        UserPassUpdateRequest wrongUserPassUpdateRequest = new UserPassUpdateRequest("wrongPass", "newPass");
        doThrow(new BadCredentialsException("비밀번호가 올바르지 않습니다."))
                .when(userService).updatePassword(eq("user1"), any(UserPassUpdateRequest.class));

        // when & then
        mockMvc.perform(put("/user/updatePassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongUserPassUpdateRequest))
                        .with(user(testUser))
                        .with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호가 올바르지 않습니다."))
                .andExpect(jsonPath("$.error").value("401"));
    }

    @Test
    @DisplayName("회원 탈퇴하기 성공")
    void deleteUser() throws Exception {
        mockMvc.perform(delete("/user/deleteUser")
                        .with(user(testUser))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원 탈퇴 성공"));
    }
}
