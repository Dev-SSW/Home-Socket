package Homepage.practice.User.Unit;

import Homepage.practice.Exception.JwtInvalid;
import Homepage.practice.Exception.UserNotFound;
import Homepage.practice.Exception.UsernameAlreadyExists;
import Homepage.practice.User.AuthService;
import Homepage.practice.User.DTO.JwtRequest;
import Homepage.practice.User.DTO.JwtResponse;
import Homepage.practice.User.DTO.LoginRequest;
import Homepage.practice.User.DTO.SignupRequest;
import Homepage.practice.User.JWT.JwtUtils;
import Homepage.practice.User.Role;
import Homepage.practice.User.User;
import Homepage.practice.User.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UnitAuthService {
    // 외부 의존성 가짜 객체
    @Mock
    private UserRepository userRepository;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private PasswordEncoder passwordEncoder;

    // 테스트 할 클래스
    @InjectMocks
    private AuthService authService;

    // 테스트 필드
    private User testUser;
    private SignupRequest signupRequest;
    private LoginRequest loginRequest;
    private JwtRequest jwtRequest;

    @BeforeEach
    void setup() {
        signupRequest = new SignupRequest("user1", "pass1", LocalDate.of(2000,1,1), "홍길동");
        loginRequest = new LoginRequest("user1", "pass1");
        jwtRequest = new JwtRequest("token1");
        testUser = User.builder()
                .id(1L)
                .username("user1")
                .password("pass1")
                .birth(LocalDate.of(2000,1,1))
                .name("홍길동")
                .role(Role.ROLE_USER)
                .build();
    }

    @Test
    @DisplayName("회원 가입 성공")
    void signup_success() {
        // given
        given(userRepository.existsByUsername("user1")).willReturn(false);
        given(passwordEncoder.encode("pass1")).willReturn("pass1");
        // when
        authService.signup(signupRequest);
        // then
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원 가입 실패 - 아이디 중복")
    void signup_fail() {
        // given
        given(userRepository.existsByUsername("user1")).willReturn(true);
        // when & then
        assertThatThrownBy(() -> authService.signup(signupRequest))
                .isInstanceOf(UsernameAlreadyExists.class)
                .hasMessageContaining("이미 존재하는 아이디");
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() {
        // given
        given(authenticationManager.authenticate(any())).willReturn(mock(Authentication.class));
        given(userRepository.findByUsername("user1")).willReturn(Optional.of(testUser));
        given(jwtUtils.generateToken(testUser)).willReturn("jwtToken");
        given(jwtUtils.generateRefreshToken(any(), any())).willReturn("refreshToken");
        // when
        JwtResponse response = authService.login(loginRequest);
        // then
        assertThat(response.getToken()).isEqualTo("jwtToken");
        assertThat(response.getRefreshToken()).isEqualTo("refreshToken");
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 유저")
    void login_fail() {
        // given
        given(authenticationManager.authenticate(any())).willThrow(
                new InternalAuthenticationServiceException("fail", new UserNotFound("아이디에 해당하는 회원이 없습니다."))
        );
        // when & then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(UserNotFound.class)
                .hasMessageContaining("아이디에 해당하는 회원이 없습니다.");
    }

    @Test
    @DisplayName("토큰 재발급 성공")
    void tokenRenew_success() {
        // given
        given(jwtUtils.extractUsername("token1")).willReturn("user1");
        given(userRepository.findByUsername("user1")).willReturn(Optional.of(testUser));
        given(jwtUtils.isTokenExpired("token1")).willReturn(false);
        given(jwtUtils.isTokenValid("token1", testUser)).willReturn(true);
        given(jwtUtils.generateToken(testUser)).willReturn("AccessToken");
        given(jwtUtils.generateRefreshToken(new HashMap<>(), testUser)).willReturn("RefreshToken");
        // when
        JwtResponse response = authService.tokenRenew(jwtRequest);
        // then
        assertThat(response.getToken()).isEqualTo("AccessToken");
        assertThat(response.getRefreshToken()).isEqualTo("RefreshToken");
    }

    @Test
    @DisplayName("토큰 재발급 실패 - 리프레시 토큰 만료")
    void tokenRenew_fail() {
        // given
        given(jwtUtils.extractUsername("token1")).willReturn("user1");
        given(userRepository.findByUsername("user1")).willReturn(Optional.of(testUser));
        given(jwtUtils.isTokenExpired("token1")).willReturn(true);
        // when & then
        assertThatThrownBy(() -> authService.tokenRenew(jwtRequest))
                .isInstanceOf(JwtInvalid.class)
                .hasMessageContaining("리프레시 토큰이 만료되었습니다.");
    }

    @Test
    @DisplayName("토큰 유효성 검사 성공")
    void validateToken_success() {
        // given
        given(jwtUtils.extractUsername("token1")).willReturn("user1");
        given(userRepository.findByUsername("user1")).willReturn(Optional.of(testUser));
        given(jwtUtils.isTokenExpired("token1")).willReturn(false);
        given(jwtUtils.isTokenValid("token1", testUser)).willReturn(true);
        // when & then
        authService.validateTest(jwtRequest); // 예외 안 터지면 성공
    }

    @Test
    @DisplayName("토큰 유효성 검사 실패 - 토큰 만료")
    void validateTest_fail() {
        // given
        given(jwtUtils.extractUsername("token1")).willReturn("user1");
        given(userRepository.findByUsername("user1")).willReturn(Optional.of(testUser));
        given(jwtUtils.isTokenExpired("token1")).willReturn(true);
        // when & then
        assertThatThrownBy(() -> authService.validateTest(jwtRequest))
                .isInstanceOf(JwtInvalid.class)
                .hasMessageContaining("토큰이 만료되었습니다.");
    }
}
