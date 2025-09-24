package Homepage.practice.Exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    /** ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 기본 예외 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
    /** 400 - DTO / @Valid 실패 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalApiResponse<?>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getDefaultMessage())
                .orElse("잘못된 요청입니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalApiResponse.fail(msg, "400"));
    }

    /** 400 - @RequestParam / @PathVariable 검증 실패 */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<GlobalApiResponse<?>> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalApiResponse.fail("요청 파라미터가 올바르지 않습니다.", "400"));
    }

    /** 400 - 필수 파라미터 누락 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<GlobalApiResponse<?>> handleMissingParam(MissingServletRequestParameterException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalApiResponse.fail("필수 파라미터가 누락되었습니다: " + ex.getParameterName(), "400"));
    }

    /** 400 - JSON 파싱 실패 등 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GlobalApiResponse<?>> handleNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalApiResponse.fail("요청 본문을 읽을 수 없습니다.", "400"));
    }

    /** 404 - 존재하지 않는 URL */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<GlobalApiResponse<?>> handleNoHandlerFound(NoHandlerFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(GlobalApiResponse.fail("존재하지 않는 경로입니다: " + ex.getRequestURL(), "404"));
    }

    /** 502 - 외부 API 호출 실패 */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<GlobalApiResponse<?>> handleExternalApi(RestClientException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(GlobalApiResponse.fail("외부 API 호출에 실패했습니다.", "502"));
    }

    /** 503 - DB/캐시 등 의존 서비스 장애 */
    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<GlobalApiResponse<?>> handleDataAccessDown(DataAccessResourceFailureException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(GlobalApiResponse.fail("현재 서비스 이용이 불가능합니다. 잠시 후 다시 시도해주세요.", "503"));
    }

    /** 500 - 그 외 모든 예외 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalApiResponse<?>> handleGeneral(Exception ex) {
        // 운영환경에서는 구체 메시지 노출 지양
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(GlobalApiResponse.fail("서버 내부 오류가 발생했습니다.", "500"));
    }

    /** ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 회원 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
    /** 401 - 로그인 오류 문제 */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<GlobalApiResponse<?>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(GlobalApiResponse.fail("비밀번호가 올바르지 않습니다.", "401"));
    }

    /** 회원 404 오류 */
    @ExceptionHandler(UserNotFound.class)
    public ResponseEntity<GlobalApiResponse<?>> handleUserNotFound(UserNotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(GlobalApiResponse.fail(ex.getMessage(), "USER_NOT_FOUND"));
    }

    /** 회원 아이디 이미 존재 오류 */
    @ExceptionHandler(UsernameAlreadyExists.class)
    public ResponseEntity<GlobalApiResponse<?>> handleUsernameAlreadyExists(UsernameAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalApiResponse.fail(ex.getMessage(), "USERNAME_EXISTS"));
    }

    /** ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ JWT ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
    /** JWT 유효성 검사 (토큰 만료, 유효성) */
    @ExceptionHandler(JwtInvalid.class)
    public ResponseEntity<GlobalApiResponse<?>> handleJwtInvalid(JwtInvalid ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(GlobalApiResponse.fail(ex.getMessage(), "JWT_INVALID"));
    }

    /** ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ OAUTH ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
    /** 사용할 수 없는 provider */
    @ExceptionHandler(OAuthProviderNotFound.class)
    public ResponseEntity<GlobalApiResponse<?>> handleOAuthProviderNotFound(OAuthProviderNotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(GlobalApiResponse.fail(ex.getMessage(), "OAUTH_PROVIDER_NOT_FOUND"));
    }

    /** ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 카테고리 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
    /** 카테고리 부모 404 오류 */
    @ExceptionHandler(CategoryParentNotFound.class)
    public ResponseEntity<GlobalApiResponse<?>> handleCategoryParentNotFound(CategoryParentNotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(GlobalApiResponse.fail(ex.getMessage(), "CATEGORY_PARENT_NOT_FOUND"));
    }

    /** 카테고리 404 오류 */
    @ExceptionHandler(CategoryNotFound.class)
    public ResponseEntity<GlobalApiResponse<?>> handleCategoryNotFound(CategoryNotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(GlobalApiResponse.fail(ex.getMessage(), "CATEGORY_NOT_FOUND"));
    }

    /** ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 아이템 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
    /** 아이템 재고 부족 오류 */
    @ExceptionHandler(ItemOutOfStockException.class)
    public ResponseEntity<GlobalApiResponse<?>> handleItemOutOfStockException(ItemOutOfStockException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalApiResponse.fail(ex.getMessage(), "ITEM_OUT_OF_STOCK"));
    }

    /** 아이템 404 오류 */
    @ExceptionHandler(ItemNotFound.class)
    public ResponseEntity<GlobalApiResponse<?>> handleItemNotFound(ItemNotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(GlobalApiResponse.fail(ex.getMessage(), "ITEM_NOT_FOUND"));
    }

    /** ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 장바구니 아이템 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
    /** 장바구니 아이템 404 오류 */
    @ExceptionHandler(CartItemNotFound.class)
    public ResponseEntity<GlobalApiResponse<?>> handleCartItemNotFound(CartItemNotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(GlobalApiResponse.fail(ex.getMessage(), "CART_ITEM_NOT_FOUND"));
    }

    /** ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 장바구니 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
    /** 장바구니 404 오류 */
    @ExceptionHandler(CartNotFound.class)
    public ResponseEntity<GlobalApiResponse<?>> handleCartNotFound(CartNotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(GlobalApiResponse.fail(ex.getMessage(), "CART_NOT_FOUND"));
    }

    /** 장바구니 소유자 오류 */
    @ExceptionHandler(CartAccessDenied.class)
    public ResponseEntity<GlobalApiResponse<?>> handleCartAccessDenied(CartAccessDenied ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(GlobalApiResponse.fail(ex.getMessage(), "CART_ACCESS_DENIED"));
    }

    /** ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 쿠폰 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
    /** 쿠폰 404 오류 */
    @ExceptionHandler(CouponNotFound.class)
    public ResponseEntity<GlobalApiResponse<?>> handleCouponNotFound(CouponNotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(GlobalApiResponse.fail(ex.getMessage(), "COUPON_NOT_FOUND"));
    }

    /** 쿠폰 이미 존재 오류 */
    @ExceptionHandler(CouponAlreadyExists.class)
    public ResponseEntity<GlobalApiResponse<?>> handleCouponAlreadyExists(CouponAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalApiResponse.fail(ex.getMessage(), "COUPON_EXISTS"));
    }

    /** 쿠폰 기한 만료 오류 */
    @ExceptionHandler(CouponExpired.class)
    public ResponseEntity<GlobalApiResponse<?>> handleCouponExpired(CouponExpired ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalApiResponse.fail(ex.getMessage(), "COUPON_EXPIRED"));
    }

    /** ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 쿠폰 발급 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
    /** 발급 쿠폰 이미 사용 오류 */
    @ExceptionHandler(CouponPublishAlreadyUsed.class)
    public ResponseEntity<GlobalApiResponse<?>> handleCouponPublishAlreadyUsed(CouponPublishAlreadyUsed ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalApiResponse.fail(ex.getMessage(), "COUPON_PUBLIC_ALREADY_USED"));
    }

    /** 발급 쿠폰 기한 만료 오류 */
    @ExceptionHandler(CouponPublishExpired.class)
    public ResponseEntity<GlobalApiResponse<?>> handleCouponPublishExpired(CouponPublishExpired ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalApiResponse.fail(ex.getMessage(), "COUPON_PUBLISH_EXPIRED"));
    }

    /** 발급 쿠폰 이미 존재 오류 */
    @ExceptionHandler(CouponPublishAlreadyExist.class)
    public ResponseEntity<GlobalApiResponse<?>> handleCouponPublishAlreadyExist(CouponPublishAlreadyExist ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(GlobalApiResponse.fail(ex.getMessage(), "COUPON_PUBLISH_EXIST"));
    }

    /** ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ 배송/주소 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ */
    /** 주소 404 오류 */
    @ExceptionHandler(AddressNotFound.class)
    public ResponseEntity<GlobalApiResponse<?>> handleAddressNotFound(AddressNotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(GlobalApiResponse.fail(ex.getMessage(), "ADDRESS_NOT_FOUND"));
    }

    /** 배송 404 오류 */
    @ExceptionHandler(DeliveryNotFound.class)
    public ResponseEntity<GlobalApiResponse<?>> handleDeliveryNotFound(DeliveryNotFound ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(GlobalApiResponse.fail(ex.getMessage(), "DELIVERY_NOT_FOUND"));
    }

}
