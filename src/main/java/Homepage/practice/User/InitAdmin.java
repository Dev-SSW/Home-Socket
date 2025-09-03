package Homepage.practice.User;

import Homepage.practice.User.DTO.SignupRequest;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class InitAdmin {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @PostConstruct
    public void initAdmin() {
        if (!userRepository.existsByUsername("admin")) {
            User user = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("0000"))
                    .birth(LocalDate.parse("2000-01-01"))
                    .name("관리자")
                    .role(Role.ROLE_ADMIN)
                    .build();
            userRepository.save(user);
        }
    }
}
