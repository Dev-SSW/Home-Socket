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

@RestControllerAdvice
public class GlobalExceptionHandler {

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

    /** 401 - 로그인 오류 문제 */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<GlobalApiResponse<?>> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(GlobalApiResponse.fail("아이디 또는 비밀번호가 올바르지 않습니다.", "401"));
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
}
