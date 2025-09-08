package Homepage.practice.User.Unit;

import Homepage.practice.Exception.UserNotFound;
import Homepage.practice.User.DTO.UserPassUpdateRequest;
import Homepage.practice.User.DTO.UserResponse;
import Homepage.practice.User.DTO.UserUpdateRequest;
import Homepage.practice.User.Role;
import Homepage.practice.User.User;
import Homepage.practice.User.UserRepository;
import Homepage.practice.User.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UnitUserService {
    // 외부 의존성 가짜 객체
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    // 테스트 할 클래스
    @InjectMocks
    private UserService userService;

    // 테스트 필드
    private User testUser;
    private User testUser2;
    private UserUpdateRequest userUpdateRequest;
    private UserPassUpdateRequest userPassUpdateRequest;

    @BeforeEach
    void setup() {
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
        testUser2 = User.builder()
                .id(1L)
                .username("user2")
                .password("pass2")
                .birth(LocalDate.of(2000,1,1))
                .name("홍길동2")
                .role(Role.ROLE_USER)
                .tokenVersion(1)
                .build();
    }

    @Test
    @DisplayName("유저 조회 성공")
    void loadUserByUsername_success() {
        // given
        given(userRepository.findByUsername("user1")).willReturn(Optional.of(testUser));
        // when
        var userDetails = userService.loadUserByUsername("user1");
        // then
        assertThat(userDetails.getUsername()).isEqualTo("user1");
    }

    @Test
    @DisplayName("유저 조회 실패 - 존재하지 않는 유저")
    void loadUserByUsername_fail() {
        // given
        given(userRepository.findByUsername("user1")).willReturn(Optional.empty());     // 에러를 발생시킴
        // when & then
        assertThatThrownBy(() -> userService.loadUserByUsername("user1"))
                .isInstanceOf(UserNotFound.class)
                .hasMessage("아이디에 해당하는 회원이 없습니다.");
    }

    @Test
    @DisplayName("전체 유저 정보 가져오기 성공")
    void getAllUser() {
        // given
        given(userRepository.findAll()).willReturn(List.of(testUser, testUser2));
        // when
        List<UserResponse> responses = userService.getAllUser();
        // then
        assertThat(responses.get(0).getUsername()).isEqualTo("user1");
        assertThat(responses.get(0).getName()).isEqualTo("홍길동");
        assertThat(responses.get(1).getUsername()).isEqualTo("user2");
        assertThat(responses.get(1).getName()).isEqualTo("홍길동2");
    }

    @Test
    @DisplayName("특정 유저 정보 가져오기 성공")
    void getUser() {
        // given
        given(userRepository.findByUsername("user1")).willReturn(Optional.of(testUser));
        // when
        UserResponse response = userService.getUser("user1");
        // then
        assertThat(response.getUsername()).isEqualTo("user1");
        assertThat(response.getName()).isEqualTo("홍길동");
    }

    @Test
    @DisplayName("유저 정보 수정하기 성공 (비밀번호 제외)")
    void updateUser() {
        // given
        given(userRepository.findByUsername("user1")).willReturn(Optional.of(testUser));
        // when
        userService.updateUser("user1", userUpdateRequest);
        // then
        assertThat(testUser.getName()).isEqualTo("송길똥");
    }

    @Test
    @DisplayName("비밀번호 수정하기 성공")
    void updatePassword_success() {
        // given
        given(userRepository.findByUsername("user1")).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("pass1", "pass1")).willReturn(true);
        given(passwordEncoder.encode("newPass")).willReturn("newPass");
        // when
        userService.updatePassword("user1", userPassUpdateRequest);
        // then
        assertThat(testUser.getPassword()).isEqualTo("newPass");
        assertThat(testUser.getTokenVersion()).isEqualTo(2);
    }

    @Test
    @DisplayName("비밀번호 수정하기 실패 - 비밀번호 오류")
    void updatePassword() {
        // given
        UserPassUpdateRequest wrongUserPassUpdateRequest = new UserPassUpdateRequest("wrongPass", "newPass");
        given(userRepository.findByUsername("user1")).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("wrongPass", "pass1")).willReturn(false);
        // when & then
        assertThatThrownBy(() -> userService.updatePassword("user1", wrongUserPassUpdateRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessageContaining("비밀번호가 올바르지 않습니다.");
    }

    @Test
    @DisplayName("회원 탈퇴하기 성공")
    void deleteUser() {
        // given
        given(userRepository.findByUsername("user1")).willReturn(Optional.of(testUser));
        // when
        userService.deleteUser("user1");
        // then
        assertThat(testUser.getTokenVersion()).isEqualTo(2); // 토큰 버전 올랐으면 실행된 것
    }
}
