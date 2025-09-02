package Homepage.practice.User;

import Homepage.practice.Exception.UserNotFound;
import Homepage.practice.User.DTO.UserPassUpdateRequest;
import Homepage.practice.User.DTO.UserResponse;
import Homepage.practice.User.DTO.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFound("아이디에 해당하는 회원이 없습니다."));
        return user;
    }

    /** 전체 유저 정보 가져오기 */
    public List<UserResponse> getAllUser() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .birth(user.getBirth())
                        .name(user.getName())
                        .role(user.getRole())
                        .build())
                .toList();
    }

    /** 특정 유저 정보 가져오기 */
    public UserResponse getUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFound("아이디에 해당하는 회원이 없습니다."));
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .birth(user.getBirth())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }

    /** 유저 정보 수정하기 (비밀번호 제외) */
    @Transactional
    public void updateUser(User user, UserUpdateRequest request) {
        user.updateUser(request);
    }

    /** 비밀번호 수정하기 */
    @Transactional
    public void updatePassword(User user, UserPassUpdateRequest request) {
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("비밀번호가 올바르지 않습니다.");
        }
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    /** 회원 탈퇴하기 */
    @Transactional
    public void deleteUser(User user) {
        userRepository.deleteById(user.getId());
    }
}
