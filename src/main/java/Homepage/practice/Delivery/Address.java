package Homepage.practice.Delivery;

import Homepage.practice.Delivery.DTO.AddressRequest;
import Homepage.practice.Delivery.DTO.AddressUpdateRequest;
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
    private String street;          // 도로명
    private String detailStreet;    // 상세 주소
    private String zipcode;         // 우편번호
    private boolean isDefault;      // 기본 배송지 여부

    /** 연관관계 */
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id")
    private User user;

    /*
    // 연관 삭제가 필요 없어 단방향으로 변경
    @OneToMany(mappedBy = "address")
    private List<Delivery> deliveries = new ArrayList<>();
    */

    /** 연관관계 편의 메서드 */
    public void setUser(User user) {
        this.user = user;
    }

    /** 생성 메서드 */
    public static Address createAddress(User user, AddressRequest request) {
        Address address = Address.builder()
                .street(request.getStreet())
                .detailStreet(request.getDetailStreet())
                .zipcode(request.getZipcode())
                .isDefault(request.isDefault())
                .build();
        user.addAddress(address);
        return address;
    }

    /** 수정 메서드 */
    public void updateAddress(AddressUpdateRequest request) {
        if (request.getStreet() != null) this.street = request.getStreet();
        if (request.getDetailStreet() != null) this.detailStreet = request.getDetailStreet();
        if (request.getZipcode() != null) this.zipcode = request.getZipcode();
    }

    /** 비즈니스 로직 */
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
