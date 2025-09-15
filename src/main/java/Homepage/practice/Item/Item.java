package Homepage.practice.Item;

import Homepage.practice.CartItem.CartItem;
import Homepage.practice.Category.Category;
import Homepage.practice.Exception.ItemOutOfStockException;
import Homepage.practice.Item.DTO.ItemRequest;
import Homepage.practice.Item.DTO.ItemUpdateRequest;
import Homepage.practice.OrderItem.OrderItem;
import Homepage.practice.Review.Review;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Item {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "item_id")
    private Long id;
    private String name;        // 상품명
    private int stock;          // 상품 재고
    private int itemPrice;      // 상품 가격
    private float avgStar;      // 별점 평균

    /** 연관관계 */
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<CartItem> cartItems = new ArrayList<>();

    /** 연관관계 편의 메서드 */
    public void addReview(Review review) {
        reviews.add(review);
        review.setItem(this);
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setItem(this);
    }

    public void addCartItem(CartItem cartItem) {
        cartItems.add(cartItem);
        cartItem.setItem(this);
    }

    /** 생성 메서드 */
    public static Item createItem(Category category, ItemRequest request) {
        return Item.builder()
                .category(category)
                .name(request.getName())
                .stock(request.getStock())
                .itemPrice(request.getItemPrice())
                .avgStar(0)
                .build();
    }

    /** 수정 메서드 */
    public Item updateItem(ItemUpdateRequest request) {
        if (request.getName() != null) {
            this.name = request.getName();
        }
        if (request.getStock() != null) {
            this.stock = request.getStock();
        }
        if (request.getItemPrice() != null) {
            this.itemPrice = request.getItemPrice();
        }
        return this;
    }

    /** 비즈니스 로직 */
    public void addStock(int quantity) {
        this.stock += quantity;
    }

    public void removeStock(int quantity) {
        int restStock = this.stock - quantity;
        if (restStock < 0) {
            throw new ItemOutOfStockException("재고가 부족합니다.");
        }
        this.stock = restStock;
    }

    public void updateAvgStar(float newStar, int reviewCount) {
        this.avgStar = ((this.avgStar * (reviewCount - 1)) + newStar) / reviewCount;
    }
}
