package Homepage.practice.User;

import Homepage.practice.User.DTO.*;
import Homepage.practice.User.JWT.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class IntegrationUser {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JwtUtils jwtUtils;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private UserUpdateRequest userUpdateRequest;
    private UserPassUpdateRequest userPassUpdateRequest;

    @BeforeEach
    void setup() {
        signupRequest = new SignupRequest("user1", "pass1", LocalDate.of(2000,1,1), "홍길동");
        loginRequest = new LoginRequest("user1", "pass1");
        userUpdateRequest = new UserUpdateRequest(LocalDate.of(1000,10,10), "송길똥");
        userPassUpdateRequest = new UserPassUpdateRequest("pass1", "newPass");
    }

    @Test
    @Transactional
    @DisplayName("회원가입 성공")
    void signup_success() throws Exception {
        mockMvc.perform(post("/public/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원가입 성공"));
    }

    @Test
    @Transactional
    @DisplayName("회원가입 실패 - 아이디 중복")
    void signup_fail() throws Exception {
        // DB에 미리 사용자 저장
        User existingUser = User.createUser(signupRequest, passwordEncoder.encode(signupRequest.getPassword()));
        userRepository.save(existingUser);

        mockMvc.perform(post("/public/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("이미 존재하는 아이디 입니다."))
                .andExpect(jsonPath("$.error").value("USERNAME_EXISTS"));
    }

    @Test
    @Transactional
    @DisplayName("로그인 성공")
    void login_success() throws Exception {
        // 회원가입
        User newUser = User.createUser(signupRequest, passwordEncoder.encode(signupRequest.getPassword()));
        userRepository.save(newUser);

        mockMvc.perform(post("/public/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("로그인 성공"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.expirationTime").exists());
    }

    @Test
    @Transactional
    @DisplayName("로그인 실패 - 존재하지 않는 유저")
    void login_fail() throws Exception {
        // DB에 미리 저장하지 않았음
        mockMvc.perform(post("/public/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("아이디에 해당하는 회원이 없습니다."))
                .andExpect(jsonPath("$.error").value("USER_NOT_FOUND"));
    }

    @Test
    @Transactional
    @DisplayName("로그인 실패 - 잘못된 비밀번호")
    void login_wrong() throws Exception {
        // 회원가입
        User newUser = User.createUser(signupRequest, passwordEncoder.encode(signupRequest.getPassword()));
        userRepository.save(newUser);
        LoginRequest wrongPassRequest = new LoginRequest("user1", "wrongPass");

        mockMvc.perform(post("/public/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPassRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호가 올바르지 않습니다."))
                .andExpect(jsonPath("$.error").value("401"));
    }

    @Test
    @Transactional
    @DisplayName("토큰 재발급 성공")
    void tokenRenew_success() throws Exception {
        // 회원가입
        User newUser = User.createUser(signupRequest, passwordEncoder.encode(signupRequest.getPassword()));
        userRepository.save(newUser);
        // 토큰 발급
        JwtRequest jwtRequest = new JwtRequest(jwtUtils.generateRefreshToken(new HashMap<>(), newUser, newUser.getTokenVersion()));

        mockMvc.perform(post("/public/tokenRenew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jwtRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("재발급 성공"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andExpect(jsonPath("$.data.expirationTime").exists());
    }

    @Test
    @Transactional
    @DisplayName("토큰 재발급 실패 - 변조된 리프레시 토큰")
    void tokenRenew_fail1() throws Exception {
        // 회원가입
        User newUser = User.createUser(signupRequest, passwordEncoder.encode(signupRequest.getPassword()));
        userRepository.save(newUser);
        // 토큰 발급
        String token = jwtUtils.generateRefreshToken(new HashMap<>(), newUser, newUser.getTokenVersion());
        String invaildToken = token.substring(0, token.length()-2) + "AB";
        JwtRequest jwtRequest = new JwtRequest(invaildToken);

        mockMvc.perform(post("/public/tokenRenew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jwtRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("유효하지 않은 리프레시 토큰입니다."))
                .andExpect(jsonPath("$.error").value("JWT_INVALID"));
    }

    @Test
    @Transactional
    @DisplayName("토큰 재발급 실패 - 리프레시 토큰 만료")
    void tokenRenew_fail2() throws Exception {
        // 회원가입
        User newUser = User.createUser(signupRequest, passwordEncoder.encode(signupRequest.getPassword()));
        userRepository.save(newUser);
        // 만료된 토큰 발급
        JwtRequest jwtRequest = new JwtRequest(jwtUtils.generateExpiredToken(new HashMap<>(), newUser, newUser.getTokenVersion()));

        mockMvc.perform(post("/public/tokenRenew")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jwtRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("리프레시 토큰이 만료되었습니다."))
                .andExpect(jsonPath("$.error").value("JWT_INVALID"));
    }

    @Test
    @Transactional
    @DisplayName("토큰 유효성 검사 성공")
    void validateToken_success() throws Exception {
        // 회원가입
        User newUser = User.createUser(signupRequest, passwordEncoder.encode(signupRequest.getPassword()));
        userRepository.save(newUser);
        // 토큰 발급
        JwtRequest jwtRequest = new JwtRequest(jwtUtils.generateToken(newUser, newUser.getTokenVersion()));

        mockMvc.perform(post("/public/validateTest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jwtRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("유효한 토큰입니다."));
    }

    @Test
    @Transactional
    @DisplayName("토큰 유효성 검사 실패 - 변조된 토큰")
    void validateToken_fail1() throws Exception {
        // 회원가입
        User newUser = User.createUser(signupRequest, passwordEncoder.encode(signupRequest.getPassword()));
        userRepository.save(newUser);
        // 토큰 발급
        String token = jwtUtils.generateToken(newUser, newUser.getTokenVersion());
        String invaildToken = token.substring(0, token.length()-2) + "AB";
        JwtRequest jwtRequest = new JwtRequest(invaildToken);

        mockMvc.perform(post("/public/validateTest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jwtRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("유효하지 않은 토큰입니다."))
                .andExpect(jsonPath("$.error").value("JWT_INVALID"));
    }

    @Test
    @Transactional
    @DisplayName("토큰 유효성 검사 실패 - 토큰 만료")
    void validateToken_fail2() throws Exception {
        // 회원가입
        User newUser = User.createUser(signupRequest, passwordEncoder.encode(signupRequest.getPassword()));
        userRepository.save(newUser);
        // 만료된 토큰 발급
        JwtRequest jwtRequest = new JwtRequest(jwtUtils.generateExpiredToken(new HashMap<>(), newUser, newUser.getTokenVersion()));

        mockMvc.perform(post("/public/validateTest")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(jwtRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("토큰이 만료되었습니다."))
                .andExpect(jsonPath("$.error").value("JWT_INVALID"));
    }

    @Test
    @Transactional
    @DisplayName("전체 유저 정보 가져오기 성공")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void getAllUser_success() throws Exception {
        // 회원가입
        User newUser = User.createUser(signupRequest, passwordEncoder.encode(signupRequest.getPassword()));
        userRepository.save(newUser);

        // 기존 실행 시 admin 계정을 추가해뒀음
        mockMvc.perform(get("/admin/getAllUser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("전체 정보 조회 성공"))
                .andExpect(jsonPath("$.data[0].username").value("admin"))
                .andExpect(jsonPath("$.data[1].username").value("user1"));
    }

    @Test
    @Transactional
    @DisplayName("특정 유저 정보 가져오기 성공")
    void getUser_success() throws Exception {
        // 회원가입
        User newUser = User.createUser(signupRequest, passwordEncoder.encode(signupRequest.getPassword()));
        userRepository.save(newUser);

        mockMvc.perform(get("/user/getUser")
                        .with(user(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("user1"));
    }

    @Test
    @Transactional
    @DisplayName("유저 정보 수정하기 성공 (비밀번호 제외)")
    void updateUser_success() throws Exception {
        // 회원가입
        User newUser = User.createUser(signupRequest, passwordEncoder.encode(signupRequest.getPassword()));
        userRepository.save(newUser);

        mockMvc.perform(put("/user/updateUser")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateRequest))
                        .with(user(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("정보 수정 성공"))
                .andExpect(jsonPath("$.data.name").value("송길똥"));
    }

    @Test
    @Transactional
    @DisplayName("비밀번호 수정하기 성공")
    void updatePassword_success() throws Exception {
        // 회원가입
        User newUser = User.createUser(signupRequest, passwordEncoder.encode(signupRequest.getPassword()));
        userRepository.save(newUser);

        mockMvc.perform(put("/user/updatePassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userPassUpdateRequest))
                        .with(user(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("비밀번호 수정 성공"));
    }

    @Test
    @Transactional
    @DisplayName("비밀번호 수정하기 실패 - 비밀번호 오류")
    void updatePassword_fail() throws Exception {
        // 회원가입
        User newUser = User.createUser(signupRequest, passwordEncoder.encode(signupRequest.getPassword()));
        userRepository.save(newUser);
        UserPassUpdateRequest wrongUserPassUpdateRequest = new UserPassUpdateRequest("wrongPass", "newPass");

        mockMvc.perform(put("/user/updatePassword")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongUserPassUpdateRequest))
                        .with(user(newUser)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("비밀번호가 올바르지 않습니다."))
                .andExpect(jsonPath("$.error").value("401"));
    }

    @Test
    @Transactional
    @DisplayName("회원 탈퇴하기 성공 - 통합")
    void deleteUser_success() throws Exception {
        // 회원가입
        User newUser = User.createUser(signupRequest, passwordEncoder.encode(signupRequest.getPassword()));
        userRepository.save(newUser);

        mockMvc.perform(delete("/user/deleteUser")
                        .with(user(newUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원 탈퇴 성공"));
    }
}
