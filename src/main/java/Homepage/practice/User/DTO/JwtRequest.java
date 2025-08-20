package Homepage.practice.User.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JwtRequest {
    @NotBlank(message = "토큰을 입력하셔야 합니다.")
    private String token;
}
