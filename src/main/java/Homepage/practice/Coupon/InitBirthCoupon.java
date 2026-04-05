package Homepage.practice.Coupon;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class InitBirthCoupon {
    private final CouponRepository couponRepository;
    @PostConstruct
    public void initBirthCoupon() {
        if(!couponRepository.existsByName("생일쿠폰")) {
            Coupon coupon = Coupon.builder()
                    .name("생일쿠폰")
                    .discount(BigDecimal.valueOf(10000))
                    .validStart(LocalDate.parse("2000-01-01"))
                    .validEnd(LocalDate.parse("2200-01-01"))
                    .afterIssue(30)
                    .build();
            couponRepository.save(coupon);
        }
    }
}
