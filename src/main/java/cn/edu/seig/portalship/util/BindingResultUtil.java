package cn.edu.seig.portalship.util;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

public class BindingResultUtil {

    public static String handleBindingResultErrors(BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder("输入参数校验失败: ");
            for (ObjectError error : bindingResult.getAllErrors()) {
                errorMessage.append(error.getDefaultMessage()).append("; ");
            }
            return errorMessage.toString();
        }
        return null;
    }
}
