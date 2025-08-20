package Homepage.practice.User;

import Homepage.practice.User.DTO.SignupRequest;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User implements UserDetails, OAuth2User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "user_id")
    private Long id;                        // id
    private String username;                // 회원 ID
    private String password;                // 회원 Password
    private LocalDate birth;                // 회원 생년월일
    private String name;                    // 회원 이름
    private Role role;                      // 회원 권한

    /** OAuth2User 구현부 */
    @Transient                              // 관리 대상에서 해당 필드나 메서드를 제외
    private Map<String, Object> attributes; // OAuth2 속성
    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    @Override
    public String getName() {
        return name;
    }
    @Override
    public String getUsername() {
        return username;
    }

    /** UserDetails 구현부 */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority(role.name()));
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return true;
    }

    /** 생성 메서드 */
    public static User createUser(SignupRequest request, PasswordEncoder passwordEncoder) {
        return User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .birth(request.getBirth())
                .name(request.getName())
                .role(Role.ROLE_USER)
                .build();
    }
}
