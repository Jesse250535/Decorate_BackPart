package com.example.decoratebackservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户完整信息（sys_user 全部字段）
 * <p>
 * 既有 User 实体未映射 email、create_time、update_time，本类补齐全部列，
 * 供「查询用户的所有信息」使用，未改动既有 User 实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户完整信息（含全部字段）")
public class UserFullInfo {

    @Schema(description = "用户ID", example = "1")
    private int id;

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "手机号", example = "13800000000")
    private String phone;

    @Schema(description = "邮箱", example = "zhangsan@example.com")
    private String email;

    @Schema(description = "密码（为安全起见返回时置空）", example = "123456")
    private String password;

    @Schema(description = "头像地址", example = "path")
    private String avatar;

    @Schema(description = "昵称", example = "张三")
    private String nickname;

    @Schema(description = "性别 0未知1男2女", example = "1")
    private int gender;

    @Schema(description = "用户身份类型 1-5", example = "1")
    private int user_type;

    @Schema(description = "关联角色ID", example = "1")
    private int role_id;

    @Schema(description = "个人简介", example = "xxx")
    private String profile;

    @Schema(description = "违规次数", example = "0")
    private int violation_count;

    @Schema(description = "账号状态 0正常1封禁", example = "0")
    private int status;

    @Schema(description = "注册时间")
    private LocalDateTime create_time;

    @Schema(description = "更新时间")
    private LocalDateTime update_time;

    @Schema(description = "逻辑删除 0未删1已删", example = "0")
    private int is_deleted;
}
