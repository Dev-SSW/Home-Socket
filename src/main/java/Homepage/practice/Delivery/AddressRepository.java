package Homepage.practice.Delivery;

import Homepage.practice.User.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    /** 유저 ID와 기본 배송지 TRUE인 주소 조회 */
    @Query("select a from Address a where a.user.id = :userId and a.defaultAddress = true")
    Address findByUserIdAndDefaultAddressTrue(@Param("userId") Long userId);

    /** 유저의 주소 개수 조회 */
    long countByUser(User user);

    /** 유저의 주소 모두 조회 */
    List<Address> findByUserId(Long userId);

    /** 유저의 주소 중 첫번째 주소를 조회 */
    @Query("select a from Address a where a.user.id = :userId order by a.id ASC")
    Address findFirstByUserIdOrderByIdAsc(@Param("userId") Long userId);
}
