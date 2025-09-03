package Homepage.practice.User.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true) // 요청에 예상치 못한 필드가 있어도 무시
public class UserUpdateRequest {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate birth;                // 회원 생년월일
    private String name;                    // 회원 이름
}
