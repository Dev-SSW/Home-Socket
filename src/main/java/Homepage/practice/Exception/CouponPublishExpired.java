package Homepage.practice.Exception;

public class CouponPublishExpired extends RuntimeException {
    public CouponPublishExpired(String message) { super(message); }
}
