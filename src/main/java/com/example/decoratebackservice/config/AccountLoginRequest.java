package com.example.decoratebackservice.config;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 昵称/邮箱登录请求
 */
@Data
@Schema(description = "昵称或邮箱登录请求")
public class AccountLoginRequest {

    @NotBlank(message = "昵称或邮箱不能为空")
    @Schema(description = "昵称或邮箱", example = "zhangsan@example.com")
    private String account;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    private String password;
}
