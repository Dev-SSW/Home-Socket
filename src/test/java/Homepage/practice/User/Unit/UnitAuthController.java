package Homepage.practice.User.Unit;

import Homepage.practice.Exception.JwtInvalid;
import Homepage.practice.Exception.UserNotFound;
import Homepage.practice.Exception.UsernameAlreadyExists;
import Homepage.practice.User.AuthController;
import Homepage.practice.User.AuthService;
import Homepage.practice.User.DTO.JwtRequest;
import Homepage.practice.User.DTO.JwtResponse;
import Homepage.practice.User.DTO.LoginRequest;
import Homepage.practice.User.DTO.SignupRequest;
import Homepage.practice.User.JWT.JwtUtils;
import Homepage.practice.User.UserRepository;
import Homepage.practice.User.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // 필터 비활성화
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
    }

    @Test
    @DisplayName("회원 가입 성공")
    void signup_success() throws Exception {
        mockMvc.perform(post("/public/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원가입 성공"));
    }

    @Test
    @DisplayName("회원 가입 실패 - 아이디 중복")
    void signup_fail() throws Exception {
        // void 메서드에 예외 던지기
        doThrow(new UsernameAlreadyExists("이미 존재하는 아이디 입니다.")).when(authService).signup(any(SignupRequest.class));

        mockMvc.perform(post("/public/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 존재하는 아이디 입니다."))
                .andExpect(jsonPath("$.error").value("USERNAME_EXISTS"));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() throws Exception {
        given(authService.login(any(LoginRequest.class))).willReturn(jwtResponse);

        mockMvc.perform(post("/public/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.data.token").value("accessToken"))
                .andExpect(jsonPath("$.data.refreshToken").value("refreshToken"))
                .andExpect(jsonPath("$.data.expirationTime").value("24Hr"));
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 유저")
    void login_fail() throws Exception {
        given(authService.login(any(LoginRequest.class))).willThrow(new UserNotFound("아이디에 해당하는 회원이 없습니다."));

        mockMvc.perform(post("/public/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 회원이 없습니다."))
                .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"));
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void tokenRenew_success() throws Exception {
        given(authService.tokenRenew(any(JwtRequest.class))).willReturn(jwtResponse);

        mockMvc.perform(post("/public/tokenRenew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jwtRequest)))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("재발급 성공"))
                .andExpect(jsonPath("$.data.token").value("accessToken"))
                .andExpect(jsonPath("$.data.refreshToken").value("refreshToken"))
                .andExpect(jsonPath("$.data.expirationTime").value("24Hr"));
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 리프레시 토큰 만료")
    void tokenRenew_fail() throws Exception {
        given(authService.tokenRenew(any(JwtRequest.class)))
                .willThrow(new JwtInvalid("리프레시 토큰이 만료되었습니다."));

        mockMvc.perform(post("/public/tokenRenew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jwtRequest)))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("리프레시 토큰이 만료되었습니다."))
                .andExpect(jsonPath("$.error").value("JWT_INVALID"));
    }

    @Test
    @DisplayName("토큰 유효성 검사 성공")
    void validateTest_success() throws Exception {
        mockMvc.perform(post("/public/validateTest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jwtRequest)))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("유효한 토큰입니다."));
    }

    @Test
    @DisplayName("토큰 유효성 검사 실패 - 토큰 만료")
    void validateTest_fail() throws Exception {
        doThrow(new JwtInvalid("토큰이 만료되었습니다."))
                .when(authService).validateTest(any(JwtRequest.class));

        mockMvc.perform(post("/public/validateTest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jwtRequest)))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("토큰이 만료되었습니다."))
                .andExpect(jsonPath("$.error").value("JWT_INVALID"));
    }
}
