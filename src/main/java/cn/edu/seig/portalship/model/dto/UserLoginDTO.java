package cn.edu.seig.portalship.model.dto;

import cn.edu.seig.portalship.constant.MessageConstant;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserLoginDTO {

    @NotBlank(message = MessageConstant.EMAIL + MessageConstant.NOT_NULL)
    @Email(message = MessageConstant.EMAIL + MessageConstant.FORMAT_ERROR)
    private String email;

    @NotBlank(message = MessageConstant.PASSWORD + MessageConstant.NOT_NULL)
    private String password;
}
