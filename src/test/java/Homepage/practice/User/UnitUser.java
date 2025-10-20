package Homepage.practice.User;

import Homepage.practice.User.DTO.UserPassUpdateRequest;
import Homepage.practice.User.DTO.UserUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UnitUser {
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;

    private User testUser;

    @BeforeEach
    void setup() {
        testUser = User.builder()
                .id(1L)
                .username("user1")
                .password("pass1")
                .birth(LocalDate.of(2000,1,1))
                .name("홍길동1")
                .role(Role.ROLE_USER)
                .tokenVersion(1)
                .build();
    }

    @Test
    @DisplayName("유저 조회 성공")
    void loadUserByUsername_success() {
        // given
        given(userRepository.findByUsername(testUser.getUsername())).willReturn(Optional.of(testUser));

        // when
        var userDetails = userService.loadUserByUsername(testUser.getUsername());

        // then
        assertThat(userDetails.getUsername()).isEqualTo(testUser.getUsername());
    }

    @Test
    @DisplayName("유저 정보 수정하기 성공 (비밀번호 제외)")
    void updateUser() {
        // given
        given(userRepository.findByUsername(testUser.getUsername())).willReturn(Optional.of(testUser));

        // when
        userService.updateUser(testUser.getUsername(), new UserUpdateRequest(LocalDate.of(2000,10,10), "홍길동2"));

        // then
        assertThat(testUser.getName()).isEqualTo("홍길동2");
    }

    @Test
    @DisplayName("비밀번호 수정하기 성공")
    void updatePassword_success() {
        // given
        given(userRepository.findByUsername(testUser.getUsername())).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches("pass1", "pass1")).willReturn(true);
        given(passwordEncoder.encode("pass2")).willReturn("pass2");

        // when
        userService.updatePassword(testUser.getUsername(), new UserPassUpdateRequest("pass1", "pass2"));

        // then
        assertThat(testUser.getPassword()).isEqualTo("pass2");
        assertThat(testUser.getTokenVersion()).isEqualTo(2);
    }

    @Test
    @DisplayName("회원 탈퇴하기 성공")
    void deleteUser() {
        // given
        given(userRepository.findByUsername(testUser.getUsername())).willReturn(Optional.of(testUser));

        // when
        userService.deleteUser(testUser.getUsername());

        // then
        assertThat(testUser.getTokenVersion()).isEqualTo(2); // 토큰 버전 올랐으면 실행된 것
    }
}
