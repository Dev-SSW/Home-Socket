package Homepage.practice.User;

import Homepage.practice.User.DTO.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /** 회원 ID로 회원 조회 */
    Optional<User> findByUsername(String username);

    /** 전체 회원 조회 */
    @Query("select new Homepage.practice.User.DTO.UserResponse(u.id, u.username, u.birth, u.name, u.role) from User u")
    Page<UserResponse> findUserPage(Pageable pageable);

    /** 회원 ID가 이미 존재하는지 여부 확인 */
    boolean existsByUsername(String username);

    /** 해당 생일을 가진 유저 찾기 */
    @Query("SELECT u FROM User u WHERE FUNCTION('MONTH', u.birth) = FUNCTION('MONTH', CURRENT_DATE) AND FUNCTION('DAY', u.birth) = FUNCTION('DAY', CURRENT_DATE)")
    List<User> findAllByBirthToday();
}
