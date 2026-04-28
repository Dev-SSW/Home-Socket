package Homepage.practice.Performance;

import Homepage.practice.Category.Category;
import Homepage.practice.Category.CategoryRepository;
import Homepage.practice.Category.DTO.CategoryRequest;
import Homepage.practice.CouponPublish.CouponPublishStatus;
import Homepage.practice.User.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Component
@Profile("performance")
public class TestDataGenerator implements CommandLineRunner {

    private static final int USER_COUNT = 10000;
    private static final int CATEGORY_DEPTH = 3;
    private static final int CATEGORY_BREADTH = 3;
    private static final int COUPON_COUNT = 100;
    private static final int ITEM_COUNT = 1000;
    private static final int ADDRESS_COUNT = 15000;
    private static final int CART_COUNT = 10000;
    private static final int REVIEW_COUNT = 50000;
    private static final int COUPON_PUBLISH_COUNT = 8000;
    private static final int ORDER_COUNT = 2000;

    /** CartItem 조회용 유저 */
    private static final int WRITE_CART_ITEMS_PER_USER = 300;
    private static final int CREATE_ORDER_USER_START_ID = 101;
    private static final int CREATE_ORDER_USER_END_ID = 108;
    private static final int DELETE_ITEMS_USER_START_ID = 201;
    private static final int DELETE_ITEMS_USER_END_ID = 208;

    /** 비교에서 데이터 분포가 달라지는 것을 막기 위한 고정 seed */
    private static final long RANDOM_SEED = 42L;                    // DB 초기화 후 다시 생성해도 똑같은 데이터를 형성

    /**  LocalDate.now() 대신 고정 날짜 사용 */
    private static final LocalDate BASE_DATE = LocalDate.of(2026, 1, 1);

    private final Random random = new Random(RANDOM_SEED);

    @Autowired private CategoryRepository categoryRepository;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        try {
            if (shouldGenerateData()) {
                generateLargeDataset();
                System.out.println("테스트 데이터 생성 완료");
            } else {
                System.out.println("데이터가 충분히 존재합니다");
            }
        } catch (Exception e) {
            System.err.println("데이터 생성 중 오류 : " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /** 모든 도메인에 대해 데이터 개수를 측정 */
    private boolean shouldGenerateData() {
        try {
            long userCount = count("\"user\"");
            long categoryCount = count("category");
            long couponCount = count("coupon");
            long itemCount = count("item");
            long addressCount = count("address");
            long cartCount = count("cart");
            long cartItemCount = count("cart_item");
            long reviewCount = count("review");
            long couponPublishCount = count("coupon_publish");
            long orderCount = count("orders");
            long deliveryCount = count("delivery");
            long orderItemCount = count("order_item");
            int ExCategoryCount = ExCategoryCount(CATEGORY_DEPTH, CATEGORY_BREADTH);

            return userCount < USER_COUNT
                    || categoryCount < ExCategoryCount
                    || couponCount < COUPON_COUNT
                    || itemCount < ITEM_COUNT
                    || addressCount < ADDRESS_COUNT
                    || cartCount < CART_COUNT
                    || cartItemCount < CART_COUNT
                    || reviewCount < REVIEW_COUNT
                    || couponPublishCount < COUPON_PUBLISH_COUNT
                    || orderCount < ORDER_COUNT
                    || deliveryCount < ORDER_COUNT
                    || orderItemCount < ORDER_COUNT;
        } catch (Exception e) {
            System.err.println("데이터 확인 중 오류: " + e.getMessage());
            throw e;
        }
    }

    /** 개수 카운트하기 */
    private long count(String tableName) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
        return count == null ? 0L : count;
    }

    /** 만들어져야 할 카테고리 개수 */
    private int ExCategoryCount(int depth, int breadth) {
        int total = 0;
        int currentLevelCount = 1;
        for (int i = 0; i < depth; i++) {
            currentLevelCount *= breadth;
            total += currentLevelCount;
        }
        return total;
    }

    // 0 이상 boundExclusive 미만의 int 난수 생성
    private int nextInt(int boundExclusive) {
        return random.nextInt(boundExclusive);
    }

    // minInclusive 이상 maxInclusive 이하의 int 난수 생성
    private int nextInt(int minInclusive, int maxInclusive) {
        if (minInclusive > maxInclusive) {
            throw new IllegalArgumentException("minInclusive > maxInclusive");
        }
        return minInclusive + random.nextInt(maxInclusive - minInclusive + 1);
    }

    // minInclusive 이상 maxInclusive 이하의 long 난수 생성
    private long nextLong(long minInclusive, long maxInclusive) {
        if (minInclusive > maxInclusive) {
            throw new IllegalArgumentException("minInclusive > maxInclusive");
        }
        long bound = maxInclusive - minInclusive + 1L;
        if (bound <= 0L) {
            throw new IllegalArgumentException("invalid long range");
        }
        // %는 음수가 나올 수 있으므로 floorMod 사용
        return minInclusive + Math.floorMod(random.nextLong(), bound);
    }

    public void generateLargeDataset() {
        System.out.println("대량 테스트 데이터 생성...");

        try {
            System.out.println("기본 데이터");
            bulkInsertUsers(USER_COUNT);
            List<Category> categories = createHierarchicalCategories(CATEGORY_DEPTH, CATEGORY_BREADTH);
            bulkInsertCoupons(COUPON_COUNT);

            System.out.println("의존 데이터");
            bulkInsertItems(ITEM_COUNT, categories);
            Map<Long, List<Long>> userAddressMap = bulkInsertAddresses(ADDRESS_COUNT);
            bulkInsertCarts(CART_COUNT);
            bulkInsertCartItems();
            bulkInsertWriteTestCartItems();

            System.out.println("복합 데이터");
            bulkInsertReviews(REVIEW_COUNT);
            updateItemAvgStars();
            bulkInsertCouponPublishes(COUPON_PUBLISH_COUNT);
            bulkInsertOrders(ORDER_COUNT, userAddressMap);
        } catch (Exception e) {
            System.err.println("데이터 생성 실패 : " + e.getMessage());
            throw e;
        }
    }

    private List<Category> createHierarchicalCategories(int depth, int breadth) {
        System.out.println("계층 카테고리 " + ExCategoryCount(depth, breadth) + "개 생성");

        if (depth != 3) {
            throw new IllegalArgumentException("depth=" + depth);
        }

        List<Category> allCategories = new ArrayList<>();
        List<Category> level1Categories = new ArrayList<>();
        for (int i = 1; i <= breadth; i++) {
            CategoryRequest request = CategoryRequest.builder()
                    .name("대분류" + i)
                    .depth(0)
                    .orderIndex(i)
                    .parentId(null)
                    .build();
            Category category = Category.createCategory(request, null);
            level1Categories.add(category);
        }
        allCategories.addAll(level1Categories);
        categoryRepository.saveAll(level1Categories);

        List<Category> level2Categories = new ArrayList<>();
        for (Category parent : level1Categories) {
            for (int i = 1; i <= breadth; i++) {
                CategoryRequest request = CategoryRequest.builder()
                        .name("중분류" + parent.getId() + "-" + i)
                        .depth(1)
                        .orderIndex(i)
                        .parentId(parent.getId())
                        .build();
                Category category = Category.createCategory(request, parent);
                level2Categories.add(category);
            }
        }
        allCategories.addAll(level2Categories);
        categoryRepository.saveAll(level2Categories);

        List<Category> level3Categories = new ArrayList<>();
        for (Category parent : level2Categories) {
            for (int i = 1; i <= breadth; i++) {
                CategoryRequest request = CategoryRequest.builder()
                        .name("소분류" + parent.getId() + "-" + i)
                        .depth(2)
                        .orderIndex(i)
                        .parentId(parent.getId())
                        .build();
                Category category = Category.createCategory(request, parent);
                level3Categories.add(category);
            }
        }
        allCategories.addAll(level3Categories);
        categoryRepository.saveAll(level3Categories);

        return allCategories;
    }

    private void bulkInsertUsers(int count) {
        System.out.println("사용자 " + count + "명 생성");

        List<Object[]> users = new ArrayList<>(count);

        // 비밀번호를 1번만 encode해서 모든 테스트 유저가 공유
        String encodedPassword = passwordEncoder.encode("password123");

        for (int i = 1; i <= count; i++) {
            users.add(new Object[]{
                    "user" + i,
                    encodedPassword,
                    LocalDate.of(1990 + (i % 30), (i % 12) + 1, (i % 28) + 1),
                    "사용자" + i,
                    Role.ROLE_USER.toString(),
                    1
            });
        }

        jdbcTemplate.batchUpdate(
                "INSERT INTO \"user\" (username, password, birth, name, role, token_version) VALUES (?, ?, ?, ?, ?, ?)",
                users
        );
    }

    private void bulkInsertItems(int count, List<Category> categories) {
        System.out.println("상품 " + count + "개 생성");

        List<Object[]> items = new ArrayList<>(count);

        for (int i = 1; i <= count; i++) {
            Category category = categories.get(nextInt(categories.size()));
            items.add(new Object[]{
                    "상품" + i,
                    nextInt(1000, 10000),
                    nextLong(10000L, 1009990L),
                    0.0f,
                    category.getId()
            });
        }

        jdbcTemplate.batchUpdate(
                "INSERT INTO item (name, stock, item_price, avg_star, category_id) VALUES (?, ?, ?, ?, ?)",
                items
        );
    }

    private void bulkInsertReviews(int count) {
        System.out.println("리뷰 " + count + "개 생성");

        List<Object[]> reviews = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            reviews.add(new Object[]{
                    "리뷰" + i,
                    "리뷰 내용 " + i,
                    1.0f + random.nextFloat() * 4.0f,
                    BASE_DATE.minusDays(nextInt(0, 364)),
                    nextLong(1L, USER_COUNT),
                    nextLong(1L, ITEM_COUNT)
            });
        }

        jdbcTemplate.batchUpdate(
                "INSERT INTO review (title, comment, star, review_date, user_id, item_id) VALUES (?, ?, ?, ?, ?, ?)",
                reviews
        );
    }

    private void bulkInsertCarts(int count) {
        System.out.println("장바구니 " + count + "개 생성");

        List<Object[]> carts = new ArrayList<>(count);

        for (long userId = 1; userId <= USER_COUNT && carts.size() < count; userId++) {
            carts.add(new Object[]{userId});
        }

        jdbcTemplate.batchUpdate(
                "INSERT INTO cart (user_id) VALUES (?)",
                carts
        );
    }

    private void bulkInsertCartItems() {
        System.out.println("장바구니 아이템 생성");

        try {
            List<Object[]> cartItems = new ArrayList<>();

            List<Long> cartIds = jdbcTemplate.queryForList("SELECT cart_id FROM cart ORDER BY cart_id", Long.class);
            Long maxItemId = jdbcTemplate.queryForObject("SELECT MAX(item_id) FROM item", Long.class);

            if (maxItemId == null) {
                throw new IllegalStateException("item 데이터가 없어 cart_item을 생성할 수 없습니다.");
            }

            for (Long cartId : cartIds) {
                int itemCount = nextInt(1, 5);

                for (int j = 0; j < itemCount; j++) {
                    cartItems.add(new Object[]{
                            cartId,
                            nextLong(1L, maxItemId),
                            nextInt(1, 10)
                    });
                }
            }

            jdbcTemplate.batchUpdate(
                    "INSERT INTO cart_item (cart_id, item_id, quantity) VALUES (?, ?, ?)",
                    cartItems
            );
            System.out.println("장바구니 아이템 " + cartItems.size() + "개 생성 완료");
        } catch (Exception e) {
            System.err.println("장바구니 아이템 생성 실패: " + e.getMessage());
            throw e;
        }
    }

    private void bulkInsertWriteTestCartItems() {
        System.out.println("write 테스트 전용 장바구니 아이템 생성");

        List<Object[]> cartItems = new ArrayList<>();

        // user101~108: createOrder 전용 cartItem
        for (long userId = CREATE_ORDER_USER_START_ID; userId <= CREATE_ORDER_USER_END_ID; userId++) {
            String username = "user" + userId;

            Long cartId = jdbcTemplate.queryForObject(
                    "SELECT c.cart_id " +
                            "FROM cart c " +
                            "JOIN \"user\" u ON c.user_id = u.user_id " +
                            "WHERE u.username = ?",
                    Long.class,
                    username
            );
            // createOrder용 item_id: 181~480 반복
            for (int i = 0; i < WRITE_CART_ITEMS_PER_USER; i++) {
                long itemId = 181 + (i % 300);

                cartItems.add(new Object[]{
                        cartId,
                        itemId,
                        1
                });
            }
        }

        // user201~208: deleteItems 전용 cartItem
        for (long userId = DELETE_ITEMS_USER_START_ID; userId <= DELETE_ITEMS_USER_END_ID; userId++) {
            String username = "user" + userId;

            Long cartId = jdbcTemplate.queryForObject(
                    "SELECT c.cart_id " +
                            "FROM cart c " +
                            "JOIN \"user\" u ON c.user_id = u.user_id " +
                            "WHERE u.username = ?",
                    Long.class,
                    username
            );

            // deleteItems용 item_id: 1~300 반복
            for (int i = 0; i < WRITE_CART_ITEMS_PER_USER; i++) {
                long itemId = 1 + (i % 300);

                cartItems.add(new Object[]{
                        cartId,
                        itemId,
                        1
                });
            }
        }

        jdbcTemplate.batchUpdate(
                "INSERT INTO cart_item (cart_id, item_id, quantity) VALUES (?, ?, ?)",
                cartItems
        );

        System.out.println("write 테스트 전용 cart_item " + cartItems.size() + "개 생성 완료");
    }

    private Map<Long, List<Long>> bulkInsertAddresses(int count) {
        System.out.println("주소 " + count + "개 생성");

        List<Object[]> addresses = new ArrayList<>(count);
        Map<Long, List<Long>> userAddressMap = new HashMap<>();

        for (long userId = 1; userId <= USER_COUNT; userId++) {
            int addressCount = nextInt(1, 3);
            List<Long> userAddresses = new ArrayList<>();

            for (int j = 0; j < addressCount && addresses.size() < count; j++) {
                long addressId = addresses.size() + 1L;
                addresses.add(new Object[]{
                        userId,
                        "주소" + addressId,
                        "상세주소" + addressId,
                        "우편번호" + String.format("%05d", addressId),
                        j == 0
                });
                userAddresses.add(addressId);
            }

            if (!userAddresses.isEmpty()) {
                userAddressMap.put(userId, userAddresses);
            }

            if (addresses.size() >= count) {
                break;
            }
        }

        jdbcTemplate.batchUpdate(
                "INSERT INTO address (user_id, street, detail_street, zipcode, default_address) VALUES (?, ?, ?, ?, ?)",
                addresses
        );

        return userAddressMap;
    }

    /** 유저의 쿠폰 미리 가져오기 */
    private Map<Long, List<Long>> loadAvailableCouponPublishIdsByUser() {
        Map<Long, List<Long>> result = new HashMap<>();

        jdbcTemplate.query(
                "SELECT user_id, coupon_publish_id FROM coupon_publish WHERE status = 'AVAILABLE' ORDER BY user_id ASC, coupon_publish_id ASC",
                rs -> {
                    Long userId = rs.getLong("user_id");
                    Long couponPublishId = rs.getLong("coupon_publish_id");
                    if (!result.containsKey(userId)) {
                        result.put(userId, new ArrayList<>());
                    }
                    result.get(userId).add(couponPublishId);
                }
        );
        return result;
    }

    private void bulkInsertOrders(int count, Map<Long, List<Long>> userAddressMap) {
        System.out.println("주문 " + count + "개 생성");

        List<Object[]> orders = new ArrayList<>(count);
        Map<Long, List<Long>> availableCouponPublishIdsByUser = loadAvailableCouponPublishIdsByUser();

        for (int i = 0; i < count; i++) {
            Long userId = nextLong(1L, USER_COUNT);

            // 주소 있는지 확인
            List<Long> userAddresses = userAddressMap.get(userId);
            if (userAddresses == null || userAddresses.isEmpty()) {
                continue;
            }

            // 해당 유저의 쿠폰 목록 조회
            List<Long> couponPublishIds = availableCouponPublishIdsByUser.getOrDefault(userId, Collections.emptyList());
            // 해당 유저가 쿠폰을 가지고 있으면 하나를 선택
            Long couponPublishId = couponPublishIds.isEmpty() ? null : couponPublishIds.get(nextInt(couponPublishIds.size()));

            orders.add(new Object[]{
                    userId,
                    couponPublishId,
                    0L,
                    BASE_DATE.minusDays(nextInt(0, 29))
            });
        }

        // 현재 최대 order_id 저장 (Insert 전)
        Long beforeMaxOrderId = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(order_id), 0) FROM orders", Long.class);
        if (beforeMaxOrderId == null) {
            beforeMaxOrderId = 0L;
        }

        jdbcTemplate.batchUpdate(
                "INSERT INTO orders (user_id, coupon_publish_id, total_price, order_date) VALUES (?, ?, ?, ?)",
                orders
        );

        // 방금 생성된 order_id 조회 (beforeMaxOrderId보다 큰 order_id만)
        List<Long> orderIds = jdbcTemplate.queryForList(
                "SELECT order_id FROM orders WHERE order_id > ? ORDER BY order_id ASC",
                Long.class,
                beforeMaxOrderId
        );

        List<Object[]> deliveries = new ArrayList<>(orderIds.size());
        List<Object[]> orderItems = new ArrayList<>();

        // orderIds 개수와 orders 개수 중 작은 값까지만 처리
        int orderDataSize = Math.min(orderIds.size(), orders.size());

        for (int i = 0; i < orderDataSize; i++) {
            // DB가 자동 생성한 order_id
            Long orderId = orderIds.get(i);
            // insert 전에 내가 만든 주문 데이터
            Object[] orderData = orders.get(i);
            // 그 주문을 만든 사용자 ID (order의 0번째 인덱스는 userId)
            Long userId = (Long) orderData[0];

            List<Long> userAddresses = userAddressMap.get(userId);
            Long addressId = userAddresses.get(nextInt(userAddresses.size()));

            deliveries.add(new Object[]{
                    orderId,
                    addressId,
                    "READY"
            });

            int itemCount = nextInt(1, 3);
            Long totalPrice = 0L;

            for (int j = 0; j < itemCount; j++) {
                Long itemId = nextLong(1L, ITEM_COUNT);
                int quantity = nextInt(1, 5);

                Long itemPrice = jdbcTemplate.queryForObject(
                        "SELECT item_price FROM item WHERE item_id = ?",
                        Long.class,
                        itemId
                );

                if (itemPrice == null) {
                    throw new IllegalStateException("item_price 조회 실패: itemId=" + itemId);
                }

                // 할인 가격까진 계산하지 않음
                totalPrice += itemPrice * quantity;

                orderItems.add(new Object[]{
                        orderId,
                        itemId,
                        quantity
                });
            }

            jdbcTemplate.update(
                    "UPDATE orders SET total_price = ? WHERE order_id = ?",
                    totalPrice,
                    orderId
            );
        }

        jdbcTemplate.batchUpdate(
                "INSERT INTO delivery (order_id, address_id, status) VALUES (?, ?, ?)",
                deliveries
        );

        jdbcTemplate.batchUpdate(
                "INSERT INTO order_item (order_id, item_id, quantity) VALUES (?, ?, ?)",
                orderItems
        );
    }

    private static class CouponInfo {
        Long id;
        LocalDate validStart;
        LocalDate validEnd;
        int afterIssue;
    }

    private void bulkInsertCouponPublishes(int count) {
        System.out.println("쿠폰 발급 " + count + "개 생성");

        List<CouponInfo> coupons = jdbcTemplate.query(
                "SELECT coupon_id, valid_start, valid_end, after_issue FROM coupon ORDER BY coupon_id ASC",
                (rs, rowNum) -> {
                    CouponInfo coupon = new CouponInfo();
                    coupon.id = rs.getLong("coupon_id");
                    coupon.validStart = rs.getDate("valid_start").toLocalDate();
                    coupon.validEnd = rs.getDate("valid_end").toLocalDate();
                    coupon.afterIssue = rs.getInt("after_issue");
                    return coupon;
                }
        );

        if (coupons.isEmpty()) {
            throw new IllegalStateException("coupon 데이터가 없어 coupon_publish를 생성할 수 없습니다.");
        }

        List<Object[]> couponPublishes = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            CouponInfo coupon = coupons.get(i % coupons.size());
            Long userId = (long) ((i % USER_COUNT) + 1);

            LocalDate validStart = BASE_DATE.minusDays(nextInt(0, 29));
            LocalDate validEnd = validStart.plusDays(coupon.afterIssue);

            if (validEnd.isAfter(coupon.validEnd)) {
                validEnd = coupon.validEnd;
            }

            if (validStart.isBefore(coupon.validStart)) {
                validStart = coupon.validStart;
            }

            couponPublishes.add(new Object[]{
                    userId,
                    coupon.id,
                    validStart,
                    validEnd,
                    CouponPublishStatus.AVAILABLE.toString()
            });
        }

        jdbcTemplate.batchUpdate(
                "INSERT INTO coupon_publish (user_id, coupon_id, valid_start, valid_end, status) VALUES (?, ?, ?, ?, ?)",
                couponPublishes
        );
    }

    private void updateItemAvgStars() {
        System.out.println("Item avgStar 일괄 계산");

        Map<Long, Double> itemAvgStars = new HashMap<>();

        jdbcTemplate.query(
                "SELECT item_id, AVG(star) as avg_star FROM review GROUP BY item_id",
                rs -> {
                    Long itemId = rs.getLong("item_id");
                    Double avgStar = rs.getDouble("avg_star");
                    itemAvgStars.put(itemId, avgStar);
                }
        );

        List<Object[]> updates = new ArrayList<>(itemAvgStars.size());
        for (Map.Entry<Long, Double> entry : itemAvgStars.entrySet()) {
            updates.add(new Object[]{entry.getValue(), entry.getKey()});
        }

        jdbcTemplate.batchUpdate(
                "UPDATE item SET avg_star = ? WHERE item_id = ?",
                updates
        );

        System.out.println("Item avgStar 계산 완료: " + itemAvgStars.size() + "개 상품");
    }

    private void bulkInsertCoupons(int count) {
        System.out.println("쿠폰 " + count + "개 생성");

        List<Object[]> coupons = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            coupons.add(new Object[]{
                    "쿠폰" + (i + 1),
                    nextLong(1000L, 10000L),
                    BASE_DATE.minusDays(nextInt(0, 29)),
                    BASE_DATE.plusYears(nextInt(1, 2)),
                    nextInt(7, 36)
            });
        }

        jdbcTemplate.batchUpdate(
                "INSERT INTO coupon (name, discount, valid_start, valid_end, after_issue) VALUES (?, ?, ?, ?, ?)",
                coupons
        );
    }
}
