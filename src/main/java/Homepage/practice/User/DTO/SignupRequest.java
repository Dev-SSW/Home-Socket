package Homepage.practice.User.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 요청에 예상치 못한 필드가 있어도 무시
public class SignupRequest {
    @NotBlank(message = "ID를 입력하셔야 합니다.")
    private String username;                // 회원 ID
    @NotBlank(message = "PASSWORD를 입력하셔야 합니다.")
    private String password;                // 회원 Password
    @NotNull(message = "생년월일을 입력하셔야 합니다.") @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth;                // 회원 생년월일
    @NotBlank(message = "이름을 입력하셔야 합니다.")
    private String name;                    // 회원 이름
}
