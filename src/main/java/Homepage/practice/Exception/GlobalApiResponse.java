package Homepage.practice.Exception;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)	// null 값은 응답에 포함하지 않음
@JsonIgnoreProperties(ignoreUnknown = true)	// 예기치 않은 필드 무시
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
