package Homepage.practice.User.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 요청에 예상치 못한 필드가 있어도 무시
public class JwtRequest {
    @NotBlank(message = "토큰을 입력하셔야 합니다.")
    private String token;
}
