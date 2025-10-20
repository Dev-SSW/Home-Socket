package Homepage.practice.User;

import Homepage.practice.TestUnitInit;
import Homepage.practice.User.DTO.JwtRequest;
import Homepage.practice.User.DTO.JwtResponse;
import Homepage.practice.User.DTO.LoginRequest;
import Homepage.practice.User.DTO.SignupRequest;
import Homepage.practice.User.JWT.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UnitAuth {
    @Mock private UserRepository userRepository;
    @Mock private AuthenticationManager authenticationManager;
    @Mock PasswordEncoder passwordEncoder;
    @Mock private JwtUtils jwtUtils;
    @InjectMocks private AuthService authService;

    @Test
    @DisplayName("회원 가입 성공")
    void signup_success() {
        // given
        given(userRepository.existsByUsername("user1")).willReturn(false);

        // when
        authService.signup(new SignupRequest("user1", "pass", LocalDate.of(2000, 1, 1), "홍길동"));

        // then
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        // given
        User testUser = TestUnitInit.createUser(1L);
        given(authenticationManager.authenticate(any())).willReturn(mock(Authentication.class));
        given(userRepository.findByUsername(testUser.getUsername())).willReturn(Optional.of(testUser));
        given(jwtUtils.generateToken(testUser, testUser.getTokenVersion())).willReturn("jwtToken");
        given(jwtUtils.generateRefreshToken(new HashMap<>(), testUser, testUser.getTokenVersion())).willReturn("refreshToken");

        // when
        JwtResponse response = authService.login(new LoginRequest("user1", "pass"));

        // then
        assertThat(response.getToken()).isEqualTo("jwtToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void tokenRenew_success() {
        // given
        User testUser = TestUnitInit.createUser(1L);
        given(jwtUtils.extractUsername("token1")).willReturn(testUser.getUsername());
        given(userRepository.findByUsername(testUser.getUsername())).willReturn(Optional.of(testUser));
        given(jwtUtils.isTokenExpired("token1")).willReturn(false);
        given(jwtUtils.isTokenValid("token1", testUser)).willReturn(true);
        given(jwtUtils.generateToken(testUser, testUser.getTokenVersion())).willReturn("AccessToken");
        given(jwtUtils.generateRefreshToken(new HashMap<>(), testUser, testUser.getTokenVersion())).willReturn("RefreshToken");

        // when
        JwtResponse response = authService.tokenRenew(new JwtRequest("token1"));

        // then
        assertThat(response.getToken()).isEqualTo("AccessToken");
        assertThat(response.getRefreshToken()).isEqualTo("RefreshToken");
    }

    @Test
    @DisplayName("토큰 유효성 검사 성공")
    void validateToken_success() {
        // given
        User testUser = TestUnitInit.createUser(1L);
        given(jwtUtils.extractUsername("token1")).willReturn("user1");
        given(userRepository.findByUsername(testUser.getUsername())).willReturn(Optional.of(testUser));
        given(jwtUtils.isTokenExpired("token1")).willReturn(false);
        given(jwtUtils.isTokenValid("token1", testUser)).willReturn(true);

        // when & then
        authService.validateTest(new JwtRequest("token1")); // 예외 안 터지면 성공
    }
}
