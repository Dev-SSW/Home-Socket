package Homepage.practice.User.DTO;

import Homepage.practice.User.Role;
import Homepage.practice.User.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // null 값 필드는 응답에서 제외
public class UserResponse {
    // 비밀번호를 제외한 필드
    private Long id;                        // id
    private String username;                // 회원 ID
    private LocalDate birth;                // 회원 생년월일
    private String name;                    // 회원 이름
    private Role role;                      // 회원 권한

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .birth(user.getBirth())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }
}
