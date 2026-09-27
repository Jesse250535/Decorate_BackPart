package com.example.decoratebackservice.config;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 前台用户注册请求
 */
@Data
@Schema(description = "用户注册请求")
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    private String password;

    /**
     * 身份类型文字：客户 / 装修爱好者 / 购房者 / 家具爱好者 / 其他
     */
    @NotBlank(message = "身份类型不能为空")
    @Schema(description = "身份类型文字：客户/装修爱好者/购房者/家具爱好者/其他", example = "购房者")
    private String identityType;

    @Schema(description = "手机号", example = "13800000000")
    private String phone;

    @Schema(description = "昵称", example = "小张")
    private String nickname;

    @Schema(description = "性别 0未知1男2女", example = "1")
    private Integer gender;
}
