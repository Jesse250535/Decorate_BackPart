package com.example.decoratebackservice.config;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员登录请求：支持用户名 / 昵称 / 手机号 作为登录账号
 */
@Data
@Schema(description = "管理员登录请求")
public class AdminLoginRequest {

    @NotBlank(message = "账号不能为空")
    @Schema(description = "登录账号（用户名 / 昵称 / 手机号）", example = "admin")
    private String account;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    private String password;
}
