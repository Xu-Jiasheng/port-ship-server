package cn.edu.seig.portalship.result;

import cn.edu.seig.portalship.constant.MessageConstant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(0, MessageConstant.OPERATION + MessageConstant.SUCCESS, data);
    }

    public static Result success() {
        return new Result(0, MessageConstant.OPERATION + MessageConstant.SUCCESS, null);
    }

    public static Result error() {
        return new Result(1, MessageConstant.OPERATION + MessageConstant.FAILED, null);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>(0, message, data);
    }

    public static Result success(String message) {
        return new Result(0, message, null);
    }

    public static Result error(String message) {
        return new Result(1, message, null);
    }
}
