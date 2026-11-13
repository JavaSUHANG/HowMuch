package kr.hs.dgsw.howmuch.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private String res;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", "입출금 내역 조회에 성공하였습니다.", data);
    }

    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>("fail", message, null);
    }
}
