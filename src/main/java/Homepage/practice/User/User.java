package Homepage.practice.User;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDate;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "user_id")
    private Long id;                // id
    private String username;        // 회원 ID
    private String password;        // 회원 Password
    private LocalDate birth;        // 회원 생년월일
    private String name;            // 회원 이름
    private Role role;              // 회원 권한
}
