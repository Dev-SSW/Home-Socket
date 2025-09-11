package Homepage.practice.Category;

import Homepage.practice.Category.DTO.CategoryRequest;
import Homepage.practice.Category.DTO.CategoryUpdateRequest;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "category_id")
    private Long id;
    private String name;        // 카테고리 명
    private int depth;          // 루트 = 0, 하위 = 1, 2, ...
    private int orderIndex;     // 정렬용 인덱스

    /** 연관관계 */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Category> children = new ArrayList<>();    // 자식 카테고리 (PK 소유)

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "parent_id")
    private Category parent;                                // 부모 카테고리

    /*
    // 연관 삭제가 필요 없어 단방향으로 변경
    @OneToMany(mappedBy = "category")
    private List<Item> items = new ArrayList<>();
    */

    /** 연관관계 편의 메서드 */
    public void addChild(Category child) {
        children.add(child);
        child.parent = this;
    }

    /** 생성 메서드 */
    public static Category createCategory(CategoryRequest request, Category parent) {
        Category category = Category.builder()
                .name(request.getName())
                .depth(request.getDepth())
                .orderIndex(request.getOrderIndex())
                .build();
        if (parent != null) {
            parent.addChild(category);
        }
        return category;
    }

    /** 수정 메서드 */
    public void updateCategory(CategoryUpdateRequest request) {
        if (request.getName() != null) this.name = request.getName();
        if (request.getOrderIndex() != null) this.orderIndex = request.getOrderIndex();
    }
}
