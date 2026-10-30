package com.example.decoratebackservice.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 管理员登录返回信息：包含从 sys_user（is_admin=1）读取的管理员相关字段与角色权限
 */
@Data
@Schema(description = "管理员登录返回信息")
public class AdminLoginResponse {

    @Schema(description = "主键ID")
    private int id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "头像地址")
    private String avatar;

    @Schema(description = "性别 0未知1男2女")
    private int gender;

    @Schema(description = "用户身份类型（前台语义，管理员默认 1）")
    private int user_type;

    @Schema(description = "关联角色ID")
    private int role_id;

    @Schema(description = "角色名称")
    private String role_name;

    @Schema(description = "角色标识")
    private String role_code;

    @Schema(description = "个人简介")
    private String profile;

    @Schema(description = "违规次数")
    private int violation_count;

    @Schema(description = "账号状态 0正常1封禁")
    private int status;

    @Schema(description = "权限标识集合（角色标识 + 权限标识）")
    private List<String> authorities;
}
