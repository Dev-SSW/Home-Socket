package Homepage.practice.Exception;

public class CouponPublishAlreadyUsed extends RuntimeException {
    public CouponPublishAlreadyUsed(String message) { super(message); }
}
