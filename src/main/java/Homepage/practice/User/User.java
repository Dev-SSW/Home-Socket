package Homepage.practice.User;

import Homepage.practice.Cart.Cart;
import Homepage.practice.CouponPublish.CouponPublish;
import Homepage.practice.Delivery.Address;
import Homepage.practice.Order.Order;
import Homepage.practice.Review.Review;
import Homepage.practice.User.DTO.SignupRequest;
import Homepage.practice.User.DTO.UserUpdateRequest;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDate;
import java.util.*;

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
    @Enumerated(EnumType.STRING)
    private Role role;                      // 회원 권한
    private Integer tokenVersion;           // 토큰 버전 추가 (jwt 토큰 버전)

    /** 연관관계 */
    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL)
    private List<Address> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL)
    private List<Cart> carts = new ArrayList<>();

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL)
    private List<CouponPublish> couponPublishes = new ArrayList<>();

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

    /** 토큰 버전 올리기 */
    public void incrementTokenVersion() {
        this.tokenVersion += 1;
    }

    /** 생성 메서드 */
    public static User createUser(SignupRequest request, String encodedPassword) {
        return User.builder()
                .username(request.getUsername())
                .password(encodedPassword)
                .birth(request.getBirth())
                .name(request.getName())
                .role(Role.ROLE_USER)
                .tokenVersion(1)
                .build();
    }

    /** OAuth 생성 메서드 */
    public static User createUser(String username, String name) {
        return User.builder()
                .username(username)
                .name(name)
                .role(Role.ROLE_USER) // OAuth 사용자의 기본 역할 설정
                .tokenVersion(1)
                .build();
    }

    /** 정보 수정 메서드 */
    public void updateUser(UserUpdateRequest request) {
        if (request.getName() != null) this.name = request.getName();
        if (request.getBirth() != null) this.birth = request.getBirth();
    }

    /** 비밀번호 수정 메서드 */
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}
