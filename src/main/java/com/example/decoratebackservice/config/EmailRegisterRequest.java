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

    /**
     * 注册时选择的系统角色：ROLE_USER / ROLE_PLAN_PROVIDER / ROLE_SUPPLIER /
     * ROLE_STOCK / ROLE_WORKER / ROLE_DESIGNER / ROLE_MANAGER / ROLE_FINANCE_STAFF
     * <p>
     * 与账号注册（/auth/register）使用同一套 sys_role，字段名保持 role_code 以对齐前端。
     */
    @Schema(description = "注册时选择的系统角色标识，如 ROLE_SUPPLIER；不传则按普通用户注册",
            example = "ROLE_SUPPLIER")
    private String role_code;

    /**
     * 旧字段：身份类型文字（客户/装修爱好者/…），仅在未传 role_code 时作为兜底
     */
    @Schema(description = "身份类型文字（可选，兼容旧前端）：客户/装修爱好者/购房者/家具爱好者/其他",
            example = "客户")
    private String identityType;
}
