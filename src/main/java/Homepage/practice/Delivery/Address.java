package Homepage.practice.Delivery;

import Homepage.practice.User.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name ="Address_id")
    private Long id;
    private String city;            // 도시명
    private String street;          // 도로명
    private String zipcode;         // 우편번호

    /** 연관관계 */
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private User user;
}
