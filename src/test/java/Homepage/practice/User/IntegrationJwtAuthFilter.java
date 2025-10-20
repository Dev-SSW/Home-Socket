package Homepage.practice.User;

import Homepage.practice.TestIntegrationInit;
import Homepage.practice.User.DTO.SignupRequest;
import Homepage.practice.User.JWT.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class IntegrationJwtAuthFilter {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private SignupRequest signupRequest;

    @BeforeEach
    void setup() {
        signupRequest = new SignupRequest("user1", "pass1", LocalDate.of(2000,1,1), "홍길동");
    }

    @Test
    @Transactional
    @DisplayName("토큰 없이 접근 → 401 (AuthenticationEntryPoint)")
    void unauthorized_Access401() throws Exception {
        mockMvc.perform(get("/user/getUser"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("인증이 필요합니다."))
                .andExpect(jsonPath("$.error").value("401"));
    }

    @Test
    @Transactional
    @DisplayName("토큰 접근 -> 권한 부족 → 403 (AccessDeniedHandler)")
    void forbidden_Access403() throws Exception {
        // 회원가입
        User testUser = TestIntegrationInit.createUser(userRepository);
        // 토큰 발급
        String jwt = jwtUtils.generateToken(testUser, testUser.getTokenVersion());

        mockMvc.perform(get("/admin/getAllUser")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("접근 권한이 없습니다."))
                .andExpect(jsonPath("$.error").value("403"));
    }

    @Test
    @Transactional
    @DisplayName("토큰 접근 -> 존재하지 않는 url 요청 -> 404 (GlobalExceptionHandler - NoHandlerFoundException)")
    void notFound_Access404() throws Exception {
        // 회원가입
        User testUser = TestIntegrationInit.createUser(userRepository);
        // 토큰 발급
        String jwt = jwtUtils.generateToken(testUser, testUser.getTokenVersion());

        mockMvc.perform(get("/user/NotFoundUrl")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("존재하지 않는 경로입니다: " + "/user/NotFoundUrl"))
                .andExpect(jsonPath("$.error").value("404"));
    }

    @Test
    @Transactional
    @DisplayName("토큰 만료 (JwtAuthFilter)")
    void tokenExpired() throws Exception {
        // 회원가입
        User testUser = TestIntegrationInit.createUser(userRepository);
        // 토큰 발급
        String jwt = jwtUtils.generateExpiredToken(new HashMap<>(), testUser, testUser.getTokenVersion());

        mockMvc.perform(get("/user/getUser")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("토큰이 만료되었습니다."))
                .andExpect(jsonPath("$.error").value("JWT_INVALID"));
    }

    @Test
    @Transactional
    @DisplayName("유효하지 않은 토큰 (JwtAuthFilter)")
    void tokenValid_fail() throws Exception {
        // 회원가입
        User testUser = TestIntegrationInit.createUser(userRepository);
        // 토큰 발급
        String token = jwtUtils.generateToken(testUser, testUser.getTokenVersion());
        String invaildToken = token.substring(0, token.length()-2) + "AB";

        mockMvc.perform(get("/user/getUser")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + invaildToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."))
                .andExpect(jsonPath("$.error").value("JWT_INVALID"));
    }

    @Test
    @Transactional
    @DisplayName("토큰 버전 불일치 (JwtAuthFilter)")
    void tokenVersion_not_match() throws Exception {
        // 회원가입
        User testUser = TestIntegrationInit.createUser(userRepository);
        // 토큰 발급
        String token = jwtUtils.generateToken(testUser, testUser.getTokenVersion());
        // 토큰 버전 올리기
        testUser.incrementTokenVersion();

        mockMvc.perform(get("/user/getUser")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("토큰이 더 이상 유효하지 않습니다. 다시 로그인해주세요."))
                .andExpect(jsonPath("$.error").value("JWT_INVALID"));
    }
}
