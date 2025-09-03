package Homepage.practice.Category;

import Homepage.practice.Item.Item;
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
    @OneToMany(mappedBy = "parent")                         // 한 개의 parent에 여러가지 children이 있다
    private List<Category> children = new ArrayList<>();    // 자식 카테고리 (PK 소유)

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "parent_id")
    private Category parent;                                // 부모 카테고리

    @OneToMany(mappedBy = "category")
    private List<Item> items = new ArrayList<>();
}
