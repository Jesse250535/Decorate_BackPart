package com.example.decoratebackservice.entity;



import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @BelongsProject:dream_house
 * @BelongsPackage:com.example.dream_house.model
 * @Author:Uestc_Xiye
 * @CreateTime:2023-12-17 16:29:49
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户实体")
public class User {

    @Schema(description = "用户ID", example = "1")
    private int id;

    @Schema(description = "用户名", example = "用户名")
    private String username;

    @Schema(description = "电话", example = "13800000000")
    private String phone;

    @Schema(description = "密码", example = "123456")
    private String password;

    @Schema(description = "头像文件地址", example = "path")
    private String avatar;

    @Schema(description = "昵称", example = "niname")
    private String nickname;

    @Schema(description = "性别", example = "1")
    private int gender;

    @Schema(description = "用户类型", example = "1")
    private int user_type;

    @Schema(description = "角色类型", example = "2")
    private int role_id;

    @Schema(description = "个人简介", example = "xxx")
    private String profile;

    @Schema(description = "违禁次数", example = "3")
    private int violation_count;

    @Schema(description = "账户状态", example = "1")
    private int status;

    @Schema(description = "逻辑删除标识", example = "1")
    private int is_deleted;


}
