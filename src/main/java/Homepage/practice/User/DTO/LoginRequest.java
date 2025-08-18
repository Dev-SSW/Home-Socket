package Homepage.practice.User.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LoginRequest {
    @NotBlank(message = "ID를 입력하셔야 합니다.")
    private String username;                // 회원 ID
    @NotBlank(message = "PASSWORD를 입력하셔야 합니다.")
    private String password;                // 회원 Password
}
