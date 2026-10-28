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
     * <p>
     * 新版注册页改为选择「系统角色」（role_code），该字段退化为可选的兼容项：
     * 传了 role_code 时以角色为准，不再依赖这里。
     */
    @Schema(description = "身份类型文字（可选，兼容旧前端）：客户/装修爱好者/购房者/家具爱好者/其他",
            example = "客户")
    private String identityType;

    /**
     * 注册时选择的系统角色：ROLE_USER / ROLE_PLAN_PROVIDER / ROLE_SUPPLIER /
     * ROLE_STOCK / ROLE_WORKER / ROLE_DESIGNER / ROLE_MANAGER / ROLE_FINANCE_STAFF
     */
    @Schema(description = "注册时选择的系统角色标识，如 ROLE_SUPPLIER；不传则按普通用户注册",
            example = "ROLE_SUPPLIER")
    private String role_code;

    @Schema(description = "手机号", example = "13800000000")
    private String phone;

    @Schema(description = "昵称", example = "小张")
    private String nickname;

    @Schema(description = "性别 0未知1男2女", example = "1")
    private Integer gender;
}
