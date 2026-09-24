package com.example.decoratebackservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理员账号实体（sys_admin）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理员账号实体")
public class SysAdmin {

    @Schema(description = "主键ID", example = "1")
    private int id;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "手机号", example = "13800000000")
    private String phone;

    @Schema(description = "加密密码", example = "123456")
    private String password;

    @Schema(description = "头像地址", example = "path")
    private String avatar;

    @Schema(description = "昵称", example = "管理员")
    private String nickname;

    @Schema(description = "性别 0未知1男2女", example = "0")
    private int gender;

    @Schema(description = "用户身份类型 1客户2装修爱好者3购房者4家具爱好者", example = "1")
    private int user_type;

    @Schema(description = "关联角色ID", example = "6")
    private int role_id;

    @Schema(description = "个人简介", example = "xxx")
    private String profile;

    @Schema(description = "违规次数", example = "0")
    private int violation_count;

    @Schema(description = "账号状态 0正常1封禁", example = "0")
    private int status;

    @Schema(description = "逻辑删除 0未删1已删", example = "0")
    private int is_deleted;
}
