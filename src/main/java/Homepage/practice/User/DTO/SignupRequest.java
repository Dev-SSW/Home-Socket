package Homepage.practice.User.DTO;

import Homepage.practice.User.Role;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignupRequest {
    @NotBlank(message = "ID를 입력하셔야 합니다.")
    private String username;                // 회원 ID
    @NotBlank@NotBlank(message = "PASSWORD를 입력하셔야 합니다.")
    private String password;                // 회원 Password
    @JsonFormat(pattern = "yyyy-MM-dd") @NotNull(message = "생년월일을 입력하셔야 합니다.")
    private LocalDate birth;                // 회원 생년월일
    @NotBlank@NotBlank(message = "이름을 입력하셔야 합니다.")
    private String name;                    // 회원 이름
}
