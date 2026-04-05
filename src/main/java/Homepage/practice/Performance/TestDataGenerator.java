package Homepage.practice.Performance;

import Homepage.practice.*;
import Homepage.practice.Cart.CartRepository;
import Homepage.practice.CartItem.CartItemRepository;
import Homepage.practice.Category.Category;
import Homepage.practice.Category.CategoryRepository;
import Homepage.practice.Category.DTO.CategoryRequest;
import Homepage.practice.Item.ItemRepository;
import Homepage.practice.Review.ReviewRepository;
import Homepage.practice.CouponPublish.CouponPublishStatus;
import Homepage.practice.User.Role;
import Homepage.practice.User.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Profile("performance")  // performance 프로필에서만 활성화
public class TestDataGenerator implements CommandLineRunner {

    @Autowired private UserRepository userRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private ReviewRepository reviewRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (shouldGenerateData()) {
            generateLargeDataset();
        }
    }

    private boolean shouldGenerateData() {
        // 이미 대량 데이터가 있으면 건너뛰기
        long currentCount = userRepository.count();
        System.out.println("현재 데이터 개수: " + currentCount);
        
        if (currentCount < 1000) {
            System.out.println("데이터가 1000개 미만이라 새로 생성합니다.");
            return true;
        } else {
            System.out.println("이미 대량 데이터가 있어 건너뜁니다.");
            return false;
        }
    }

    public void generateLargeDataset() {
        System.out.println("대량 테스트 데이터 생성 시작");
        
        // 계층적 카테고리 27개 생성 (3x3x3 구조)
        List<Category> categories = createHierarchicalCategories(3, 3);
        
        // 벌크 INSERT로 쿠폰 100개 생성
        bulkInsertCoupons(100);
        
        // 3. 벌크 INSERT로 사용자 10,000명 생성
        bulkInsertUsers(10000);
        
        // 3. 벌크 INSERT로 아이템 1,000개 생성
        bulkInsertItems(1000, categories);
        
        // 벌크 INSERT로 리뷰 50,000개 생성
        bulkInsertReviews(50000);
        
        // 리뷰 생성 후 Item avgStar 일괄 계산
        updateItemAvgStars();
        
        // 벌크 INSERT로 장바구니 10,000개 생성 (모든 사용자)
        bulkInsertCarts(10000);
        
        // 벌크 INSERT로 주소 15,000개 생성 (사용자당 1.5개)
        Map<Long, List<Long>> userAddressMap = bulkInsertAddresses(15000);
        
        // 벌크 INSERT로 쿠폰 발급 8,000개 생성
        bulkInsertCouponPublishes(8000);
        
        // 벌크 INSERT로 주문 2,000개 생성
        bulkInsertOrders(2000, userAddressMap);
        
        System.out.println("대량 테스트 데이터 생성 완료");
    }
    
    private List<Category> createHierarchicalCategories(int depth, int breadth) {
        System.out.println("계층적 카테고리 " + (depth * breadth) + "개 생성");
        
        List<Category> allCategories = new ArrayList<>();
        
        // 최상위 카테고리
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
        
        // 중간 카테고리
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
        
        // 최하위 카테고리
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
        System.out.println("사용자 " + count + "명 벌크 생성");
        
        List<Object[]> users = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            users.add(new Object[]{
                "user" + i,
                passwordEncoder.encode("password123"),  // 암호화
                LocalDate.of(1990 + (i % 30), (i % 12) + 1, (i % 28) + 1),
                "사용자" + i,
                Role.ROLE_USER.toString(),  // 문자열로 변환
                0                                           // token_version (int)
            });
        }
        
        jdbcTemplate.batchUpdate(
            "INSERT INTO user (username, password, birth, name, role, token_version) VALUES (?, ?, ?, ?, ?, ?)",
            users
        );
    }

    private void bulkInsertItems(int count, List<Category> categories) {
        System.out.println("상품 " + count + "개 벌크 생성");
        
        List<Object[]> items = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        for (int i = 1; i <= count; i++) {
            Category category = categories.get(random.nextInt(categories.size()));
            items.add(new Object[]{
                "상품" + i,
                random.nextInt(1000) + 1,
                (random.nextInt(100000) + 1000) * 10L,
                0.0f,                                                           // avgStar (float)
                category.getId()
            });
        }
        
        jdbcTemplate.batchUpdate(
            "INSERT INTO item (name, stock, item_price, avg_star, category_id) VALUES (?, ?, ?, ?, ?)",
            items
        );
    }

    private void bulkInsertReviews(int count) {
        System.out.println("리뷰 " + count + "개 벌크 생성");
        
        List<Object[]> reviews = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        for (int i = 0; i < count; i++) {
            reviews.add(new Object[]{
                "리뷰" + i,
                "리뷰 내용 " + i,
                random.nextFloat() * 4 + 1,      // 1.0-5.0
                LocalDate.now().minusDays(random.nextInt(365)),
                random.nextLong(1, 10000),    // user_id
                random.nextLong(1, 1000)       // item_id
            });
        }
        
        jdbcTemplate.batchUpdate(
            "INSERT INTO review (title, comment, star, review_date, user_id, item_id) VALUES (?, ?, ?, ?, ?, ?)",
            reviews
        );
    }

    private void bulkInsertCarts(int count) {
        System.out.println("장바구니 " + count + "개 벌크 생성");
        
        List<Object[]> carts = new ArrayList<>();
        
        // 각 사용자당 1개 장바구니 생성 (중복 X)
        for (long userId = 1; userId <= 10000 && carts.size() < count; userId++) {
            carts.add(new Object[]{userId});  // user_id
        }
        
        jdbcTemplate.batchUpdate(
            "INSERT INTO cart (user_id) VALUES (?)",
            carts
        );
        
        // 생성된 장바구니 ID를 가져와서 장바구니 아이템 생성
        bulkInsertCartItems(count);
    }

    private void bulkInsertCartItems(int cartCount) {
        System.out.println("장바구니 아이템 벌크 생성");
        
        List<Object[]> cartItems = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        // 생성된 장바구니 ID를 가져와서 아이템 생성
        String sql = "SELECT cart_id FROM cart ORDER BY cart_id LIMIT ?";
        List<Long> cartIds = jdbcTemplate.queryForList(sql, Long.class, cartCount);
        
        // 각 장바구니당 1-5개 아이템 생성
        for (int i = 0; i < cartIds.size(); i++) {
            Long cartId = cartIds.get(i);
            int itemCount = random.nextInt(5) + 1; // 1-5개 아이템
            
            for (int j = 0; j < itemCount; j++) {
                cartItems.add(new Object[]{
                    cartId,                                         // 실제 cart_id
                    random.nextLong(1, 1000),         // item_id
                    random.nextInt(10) + 1               // quantity
                });
            }
        }
        
        jdbcTemplate.batchUpdate(
            "INSERT INTO cart_item (cart_id, item_id, quantity) VALUES (?, ?, ?)",
            cartItems
        );
    }
    
    private Map<Long, List<Long>> bulkInsertAddresses(int count) {
        System.out.println("주소 " + count + "개 벌크 생성");
        
        List<Object[]> addresses = new ArrayList<>();
        Map<Long, List<Long>> userAddressMap = new HashMap<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        // 각 사용자별로 주소 생성 (1-3개씩)
        for (long userId = 1; userId <= 10000; userId++) {
            int addressCount = random.nextInt(3) + 1;
            List<Long> userAddresses = new ArrayList<>();
            
            for (int j = 0; j < addressCount && addresses.size() < count; j++) {
                long addressId = addresses.size() + 1;
                addresses.add(new Object[]{
                    userId,                                                             // user_id
                    "주소" + addressId,
                    "상세주소" + addressId,
                    "우편번호" + String.format("%05d", addressId),
                    j == 0                                                              // 첫 번째 주소만 기본 주소
                });
                userAddresses.add(addressId);
            }
            
            if (!userAddresses.isEmpty()) {
                userAddressMap.put(userId, userAddresses);
            }
        }
        
        jdbcTemplate.batchUpdate(
            "INSERT INTO address (user_id, street, detail_street, zipcode, default_address) VALUES (?, ?, ?, ?, ?)",
            addresses
        );
        
        return userAddressMap;
    }
    
    private void bulkInsertOrders(int count, Map<Long, List<Long>> userAddressMap) {
        System.out.println("주문 " + count + "개 벌크 생성");
        
        List<Object[]> orders = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();

        for (int i = 0; i < count; i++) {
            Long userId = random.nextLong(1, 10000);
            
            // 해당 사용자의 주소만 선택
            List<Long> userAddresses = userAddressMap.get(userId);
            if (userAddresses == null || userAddresses.isEmpty()) {
                // 주소가 없는 사용자는 건너뛰기
                continue;
            }
            Long addressId = userAddresses.get(random.nextInt(userAddresses.size()));
            
            // 해당 사용자에게 발급된 쿠폰만 사용 가능
            Long couponPublishId = null;
            try {
                String couponSql = "SELECT coupon_publish_id FROM coupon_publish WHERE user_id = ? AND status = 'AVAILABLE' ORDER BY RAND() LIMIT 1";
                couponPublishId = jdbcTemplate.queryForObject(couponSql, Long.class, userId);
            } catch (Exception e) {
                // 쿠폰이 없으면 null 유지
            }
            
            orders.add(new Object[]{
                userId,
                couponPublishId,
                0L, // totalPrice는 나중에 계산
                LocalDate.now().minusDays(random.nextInt(30))
            });
        }
        
        // Order 저장 후 실제 ID 가져오기
        jdbcTemplate.batchUpdate(
            "INSERT INTO orders (user_id, coupon_publish_id, total_price, order_date) VALUES (?, ?, ?, ?)",
            orders
        );
        
        // Order ID 조회
        List<Long> orderIds = jdbcTemplate.queryForList(
            "SELECT order_id FROM orders ORDER BY order_id DESC LIMIT ?", 
            Long.class, 
            count
        );
        
        // Delivery와 OrderItem 생성
        List<Object[]> deliveries = new ArrayList<>();
        List<Object[]> orderItems = new ArrayList<>();
        
        for (int i = 0; i < orderIds.size(); i++) {
            Long orderId = orderIds.get(i);

            Object[] orderData = orders.get(i);
            Long userId = (Long) orderData[0];

            List<Long> userAddresses = userAddressMap.get(userId);
            Long addressId = userAddresses.get(random.nextInt(userAddresses.size()));
            
            // Delivery 생성
            deliveries.add(new Object[]{
                orderId,
                addressId,
                "READY"
            });
            
            // OrderItem 생성 및 totalPrice 계산
            int itemCount = random.nextInt(3) + 1;
            Long totalPrice = 0L;
            
            for (int j = 0; j < itemCount; j++) {
                Long itemId = random.nextLong(1, 1000);
                int quantity = random.nextInt(5) + 1;
                
                // item 가격 조회
                Long itemPrice = jdbcTemplate.queryForObject(
                    "SELECT item_price FROM item WHERE item_id = ?", Long.class, itemId);
                
                totalPrice += itemPrice * quantity;
                
                orderItems.add(new Object[]{
                    orderId,
                    itemId,
                    quantity
                });
            }
            
            // totalPrice 업데이트
            jdbcTemplate.update(
                "UPDATE orders SET total_price = ? WHERE order_id = ?",
                totalPrice, orderId
            );
        }
        
        // Delivery와 OrderItem 저장
        jdbcTemplate.batchUpdate(
            "INSERT INTO delivery (order_id, address_id, status) VALUES (?, ?, ?)",
            deliveries
        );
        
        jdbcTemplate.batchUpdate(
            "INSERT INTO order_item (order_id, item_id, quantity) VALUES (?, ?, ?)",
            orderItems
        );
    }
    
    private void bulkInsertCouponPublishes(int count) {
        System.out.println("쿠폰 발급 " + count + "개 벌크 생성");
        
        List<Object[]> couponPublishes = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        // 쿠폰 정보 조회하여 유효기간 기반으로 생성
        String couponSql = "SELECT coupon_id, valid_start, valid_end, after_issue FROM coupon ORDER BY RAND() LIMIT ?";
        List<CouponInfo> coupons = jdbcTemplate.query(couponSql, new Object[]{count}, (rs, rowNum) -> {
            CouponInfo coupon = new CouponInfo();
            coupon.id = rs.getLong("coupon_id");
            coupon.validStart = rs.getDate("valid_start").toLocalDate();
            coupon.validEnd = rs.getDate("valid_end").toLocalDate();
            coupon.afterIssue = rs.getInt("after_issue");
            return coupon;
        });
        
        // CouponPublish 생성
        for (CouponInfo coupon : coupons) {
            LocalDate validStart = LocalDate.now().minusDays(random.nextInt(30));
            LocalDate validEnd = coupon.validStart.plusDays(coupon.afterIssue);
            
            // 유효기간 검증
            if (validEnd.isAfter(coupon.validEnd)) {
                // 쿠폰 만료일을 넘지 않도록
                validEnd = coupon.validEnd;
            }
            
            couponPublishes.add(new Object[]{
                random.nextLong(1, 10000),                                  // user_id
                coupon.id,                                                             // coupon_id
                validStart,                                                             // valid_start
                validEnd,                                                              // valid_end
                CouponPublishStatus.AVAILABLE.toString()       // status
            });
        }
        
        jdbcTemplate.batchUpdate(
            "INSERT INTO coupon_publish (user_id, coupon_id, valid_start, valid_end, status) VALUES (?, ?, ?, ?, ?)",
            couponPublishes
        );
    }
    
    // 쿠폰 정보
    private static class CouponInfo {
        Long id;
        LocalDate validStart;
        LocalDate validEnd;
        int afterIssue;
    }
    
    private void updateItemAvgStars() {
        System.out.println("Item avgStar 일괄 계산");
        
        // 각 Item별 리뷰 평균 계산
        Map<Long, Double> itemAvgStars = new HashMap<>();
        
        jdbcTemplate.query(
            "SELECT item_id, AVG(star) as avg_star FROM review GROUP BY item_id",
            (rs) -> {
                Long itemId = rs.getLong("item_id");
                Double avgStar = rs.getDouble("avg_star");
                itemAvgStars.put(itemId, avgStar);
            }
        );
        
        // avgStar 일괄 업데이트
        List<Object[]> updates = new ArrayList<>();
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
        System.out.println("쿠폰 " + count + "개 벌크 생성");
        
        List<Object[]> coupons = new ArrayList<>();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        
        for (int i = 0; i < count; i++) {
            coupons.add(new Object[]{
                "쿠폰" + (i + 1),
                (random.nextInt(100000) + 1000) * 10L,                   // discount (할인액)
                LocalDate.now().minusDays(random.nextInt(30)),    // valid_start
                LocalDate.now().plusYears(random.nextInt(2) + 1),  // valid_end
                random.nextInt(30) + 7                                            // after_issue (발급 후 유효 기간)
            });
        }
        
        jdbcTemplate.batchUpdate(
            "INSERT INTO coupon (name, discount, valid_start, valid_end, after_issue) VALUES (?, ?, ?, ?, ?)",
            coupons
        );
    }
}
