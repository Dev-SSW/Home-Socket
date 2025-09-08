package Homepage.practice.User.Unit;

import Homepage.practice.Exception.UserNotFound;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class UnitUserService {
    // 외부 의존성 가짜 객체
    @Mock
    private UserRepository userRepository;

    // 테스트 할 클래스
    @InjectMocks
    private UserService userService;

    // 테스트 필드
    private User testUser;

    @BeforeEach
    void setup() {
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
}
