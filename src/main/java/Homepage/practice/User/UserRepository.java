package Homepage.practice.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /** 회원 ID로 회원 조회 */
    Optional<User> findByUsername(String username);

    /** 회원 ID가 이미 존재하는지 여부 확인 */
    boolean existsByUsername(String username);
}
