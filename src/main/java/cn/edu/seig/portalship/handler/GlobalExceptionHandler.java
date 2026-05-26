package cn.edu.seig.portalship.handler;

import cn.edu.seig.portalship.constant.MessageConstant;
import cn.edu.seig.portalship.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLIntegrityConstraintViolationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Pattern DUPLICATE_ENTRY_PATTERN = Pattern.compile("Duplicate entry '(.+?)' for key '(.+?)'");

    private Result errorResult(String message) {
        return Result.error(message);
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result handleSqlIntegrityConstraintViolationException(SQLIntegrityConstraintViolationException ex) {
        log.error("SQL异常：{}", ex.getMessage(), ex);
        Matcher matcher = DUPLICATE_ENTRY_PATTERN.matcher(ex.getMessage());
        try {
            if (matcher.find()) {
                String msg = matcher.group(2) + " " + MessageConstant.ALREADY_EXISTS;
                return errorResult(msg);
            }
        } catch (IndexOutOfBoundsException e) {
            log.error("解析SQL异常时发生错误：{}", e.getMessage(), e);
        }
        return errorResult(MessageConstant.UNKNOWN_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        log.error("参数校验异常：{}", ex.getMessage(), ex);
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                .orElse("参数校验失败");
        return errorResult(errorMessage);
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleDataAccessException(DataAccessException ex) {
        log.error("数据库访问异常：{}", ex.getMessage(), ex);
        return errorResult(MessageConstant.UNKNOWN_ERROR);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result handleException(Exception ex) {
        log.error("系统异常：{}", ex.getMessage(), ex);
        return errorResult(MessageConstant.UNKNOWN_ERROR);
    }
}
