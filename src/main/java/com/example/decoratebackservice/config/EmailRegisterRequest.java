package com.example.decoratebackservice.config;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 邮箱验证码注册请求
 */
@Data
@Schema(description = "邮箱验证码注册请求")
public class EmailRegisterRequest {

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "注册邮箱", example = "user@example.com")
    private String email;

    @NotBlank(message = "验证码不能为空")
    @Schema(description = "邮箱收到的验证码", example = "123456")
    private String code;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码长度至少 6 位")
    @Schema(description = "登录密码", example = "123456")
    private String password;

    @Schema(description = "用户名，可不填；不填时取邮箱 @ 前缀自动生成", example = "zhangsan")
    private String username;

    @Schema(description = "昵称", example = "小张")
    private String nickname;

    @Schema(description = "身份类型文字：客户/装修爱好者/购房者/家具爱好者/其他，默认客户", example = "客户")
    private String identityType;
}
