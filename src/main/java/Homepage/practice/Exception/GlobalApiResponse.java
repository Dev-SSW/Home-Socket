package Homepage.practice.Exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GlobalApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String error;

    public static <T> GlobalApiResponse<T> success(String message, T data) {
        return new GlobalApiResponse<>(true, message, data, null);
    }

    public static <T> GlobalApiResponse<T> fail(String message, String errorCode) {
        return new GlobalApiResponse<>(false, message, null, errorCode);
    }
}
