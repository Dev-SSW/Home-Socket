package Homepage.practice.Performance;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
@Profile("demo")
public class DemoDataGenerator implements CommandLineRunner {
    private static final int USER_COUNT = 20;
    private static final int ITEM_COUNT = 30;
    private static final int REVIEW_COUNT = 45;
    private static final int ORDER_COUNT = 10;
    private static final LocalDate BASE_DATE = LocalDate.of(2026, 1, 1);

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random(42L);

    public DemoDataGenerator(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (hasDemoData()) {
            System.out.println("데모 데이터가 이미 존재합니다.");
            return;
        }

        List<Long> userIds = insertUsers();
        List<Long> categoryIds = insertCategories();
        List<Long> itemIds = insertItems(categoryIds);
        Map<Long, Long> addressIdsByUserId = insertAddresses(userIds);
        Map<Long, Long> cartIdsByUserId = insertCarts(userIds);
        insertCartItems(userIds, cartIdsByUserId, itemIds);
        List<Long> couponIds = insertCoupons();
        Map<Long, Long> couponPublishIdsByUserId = insertCouponPublishes(userIds, couponIds);
        insertReviews(userIds, itemIds);
        updateItemAvgStars();
        insertOrders(userIds, itemIds, addressIdsByUserId, couponPublishIdsByUserId);

        System.out.println("데모 데이터 생성 완료");
    }

    private boolean hasDemoData() {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM \"user\" WHERE username LIKE 'demo-user%'",
                Integer.class
        );
        return count != null && count > 0;
    }

    private List<Long> insertUsers() {
        List<Long> userIds = new ArrayList<>(USER_COUNT);
        String encodedPassword = passwordEncoder.encode("demo1234");

        for (int i = 1; i <= USER_COUNT; i++) {
            Long userId = jdbcTemplate.queryForObject(
                    """
                    INSERT INTO "user" (username, password, birth, name, role, token_version)
                    VALUES (?, ?, ?, ?, ?, ?)
                    RETURNING user_id
                    """,
                    Long.class,
                    "demo-user" + i,
                    encodedPassword,
                    LocalDate.of(1990 + (i % 20), (i % 12) + 1, (i % 28) + 1),
                    "데모사용자" + i,
                    "ROLE_USER",
                    1
            );
            userIds.add(userId);
        }

        return userIds;
    }

    private List<Long> insertCategories() {
        List<Long> leafCategoryIds = new ArrayList<>();

        for (int root = 1; root <= 3; root++) {
            Long rootId = insertCategory("데모 대분류 " + root, 0, root, null);

            for (int middle = 1; middle <= 2; middle++) {
                Long middleId = insertCategory("데모 중분류 " + root + "-" + middle, 1, middle, rootId);

                for (int leaf = 1; leaf <= 2; leaf++) {
                    Long leafId = insertCategory("데모 소분류 " + root + "-" + middle + "-" + leaf, 2, leaf, middleId);
                    leafCategoryIds.add(leafId);
                }
            }
        }

        return leafCategoryIds;
    }

    private Long insertCategory(String name, int depth, int orderIndex, Long parentId) {
        return jdbcTemplate.queryForObject(
                """
                INSERT INTO category (name, depth, order_index, parent_id)
                VALUES (?, ?, ?, ?)
                RETURNING category_id
                """,
                Long.class,
                name,
                depth,
                orderIndex,
                parentId
        );
    }

    private List<Long> insertItems(List<Long> categoryIds) {
        List<Long> itemIds = new ArrayList<>(ITEM_COUNT);

        for (int i = 1; i <= ITEM_COUNT; i++) {
            Long categoryId = categoryIds.get((i - 1) % categoryIds.size());
            Long itemId = jdbcTemplate.queryForObject(
                    """
                    INSERT INTO item (name, stock, item_price, avg_star, category_id)
                    VALUES (?, ?, ?, ?, ?)
                    RETURNING item_id
                    """,
                    Long.class,
                    "데모상품" + i,
                    50 + (i * 3),
                    5_000 + (i * 1_000),
                    0.0f,
                    categoryId
            );
            itemIds.add(itemId);
        }

        return itemIds;
    }

    private Map<Long, Long> insertAddresses(List<Long> userIds) {
        Map<Long, Long> addressIdsByUserId = new HashMap<>();

        for (int i = 0; i < userIds.size(); i++) {
            Long userId = userIds.get(i);
            Long addressId = jdbcTemplate.queryForObject(
                    """
                    INSERT INTO address (user_id, street, detail_street, zipcode, default_address)
                    VALUES (?, ?, ?, ?, ?)
                    RETURNING address_id
                    """,
                    Long.class,
                    userId,
                    "서울시 데모구 " + (i + 1),
                    "데모아파트 " + (100 + i) + "호",
                    String.format("%05d", 10000 + i),
                    true
            );
            addressIdsByUserId.put(userId, addressId);
        }

        return addressIdsByUserId;
    }

    private Map<Long, Long> insertCarts(List<Long> userIds) {
        Map<Long, Long> cartIdsByUserId = new HashMap<>();

        for (Long userId : userIds) {
            Long cartId = jdbcTemplate.queryForObject(
                    "INSERT INTO cart (user_id) VALUES (?) RETURNING cart_id",
                    Long.class,
                    userId
            );
            cartIdsByUserId.put(userId, cartId);
        }

        return cartIdsByUserId;
    }

    private void insertCartItems(List<Long> userIds, Map<Long, Long> cartIdsByUserId, List<Long> itemIds) {
        for (int userIndex = 0; userIndex < userIds.size(); userIndex++) {
            Long cartId = cartIdsByUserId.get(userIds.get(userIndex));

            for (int itemOffset = 0; itemOffset < 2; itemOffset++) {
                Long itemId = itemIds.get((userIndex + itemOffset) % itemIds.size());
                jdbcTemplate.update(
                        "INSERT INTO cart_item (cart_id, item_id, quantity) VALUES (?, ?, ?)",
                        cartId,
                        itemId,
                        itemOffset + 1
                );
            }
        }
    }

    private List<Long> insertCoupons() {
        List<Long> couponIds = new ArrayList<>();

        for (int i = 1; i <= 4; i++) {
            Long couponId = jdbcTemplate.queryForObject(
                    """
                    INSERT INTO coupon (name, discount, valid_start, valid_end, after_issue)
                    VALUES (?, ?, ?, ?, ?)
                    RETURNING coupon_id
                    """,
                    Long.class,
                    "데모쿠폰" + i,
                    1_000 * i,
                    BASE_DATE.minusDays(30),
                    BASE_DATE.plusYears(1),
                    30
            );
            couponIds.add(couponId);
        }

        return couponIds;
    }

    private Map<Long, Long> insertCouponPublishes(List<Long> userIds, List<Long> couponIds) {
        Map<Long, Long> couponPublishIdsByUserId = new HashMap<>();

        for (int i = 0; i < userIds.size(); i++) {
            Long userId = userIds.get(i);
            Long couponId = couponIds.get(i % couponIds.size());
            Long couponPublishId = jdbcTemplate.queryForObject(
                    """
                    INSERT INTO coupon_publish (user_id, coupon_id, valid_start, valid_end, status)
                    VALUES (?, ?, ?, ?, ?)
                    RETURNING coupon_publish_id
                    """,
                    Long.class,
                    userId,
                    couponId,
                    BASE_DATE.minusDays(3),
                    BASE_DATE.plusDays(30),
                    "AVAILABLE"
            );
            couponPublishIdsByUserId.put(userId, couponPublishId);
        }

        return couponPublishIdsByUserId;
    }

    private void insertReviews(List<Long> userIds, List<Long> itemIds) {
        for (int i = 0; i < REVIEW_COUNT; i++) {
            jdbcTemplate.update(
                    """
                    INSERT INTO review (title, comment, star, review_date, user_id, item_id)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """,
                    "데모리뷰" + (i + 1),
                    "데모 리뷰 내용 " + (i + 1),
                    3.0f + random.nextFloat() * 2.0f,
                    BASE_DATE.minusDays(random.nextInt(60)),
                    userIds.get(i % userIds.size()),
                    itemIds.get((i * 3) % itemIds.size())
            );
        }
    }

    private void updateItemAvgStars() {
        jdbcTemplate.update(
                """
                UPDATE item i
                SET avg_star = review_avg.avg_star
                FROM (
                    SELECT item_id, AVG(star) AS avg_star
                    FROM review
                    GROUP BY item_id
                ) review_avg
                WHERE i.item_id = review_avg.item_id
                """
        );
    }

    private void insertOrders(List<Long> userIds,
                              List<Long> itemIds,
                              Map<Long, Long> addressIdsByUserId,
                              Map<Long, Long> couponPublishIdsByUserId) {
        for (int i = 0; i < ORDER_COUNT; i++) {
            Long userId = userIds.get(i % userIds.size());
            Long orderId = jdbcTemplate.queryForObject(
                    """
                    INSERT INTO orders (user_id, coupon_publish_id, total_price, order_date, status)
                    VALUES (?, ?, ?, ?, ?)
                    RETURNING order_id
                    """,
                    Long.class,
                    userId,
                    couponPublishIdsByUserId.get(userId),
                    0,
                    BASE_DATE.minusDays(i),
                    "PAID"
            );

            long totalPrice = 0L;
            for (int itemOffset = 0; itemOffset < 2; itemOffset++) {
                Long itemId = itemIds.get((i + itemOffset) % itemIds.size());
                int quantity = itemOffset + 1;
                Long itemPrice = jdbcTemplate.queryForObject(
                        "SELECT item_price FROM item WHERE item_id = ?",
                        Long.class,
                        itemId
                );

                totalPrice += itemPrice * quantity;
                jdbcTemplate.update(
                        "INSERT INTO order_item (order_id, item_id, quantity) VALUES (?, ?, ?)",
                        orderId,
                        itemId,
                        quantity
                );
            }

            jdbcTemplate.update("UPDATE orders SET total_price = ? WHERE order_id = ?", totalPrice, orderId);
            jdbcTemplate.update(
                    "INSERT INTO delivery (order_id, address_id, status) VALUES (?, ?, ?)",
                    orderId,
                    addressIdsByUserId.get(userId),
                    "READY"
            );
        }
    }
}
