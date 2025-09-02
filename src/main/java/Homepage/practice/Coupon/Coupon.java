package Homepage.practice.Coupon;

import Homepage.practice.CouponPublish.CouponPublish;
import Homepage.practice.Order.Order;
import Homepage.practice.User.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "coupon_id")
    private Long id;
    private String name;            // 쿠폰 명
    private BigDecimal discount;    // 할인 가격
    private LocalDate validStart;   // 쿠폰 자체 사용 기한 (고정형 시작)
    private LocalDate validEnd;     // 쿠폰 자체 사용 기한 (고정형 만료)
    private LocalDate afterIssue;   // 유저 발급 후 유효 기간 (상대형 만료)

    @OneToMany(mappedBy = "coupon",cascade = CascadeType.ALL)
    private List<CouponPublish> couponPublishes = new ArrayList<>();
}
