package com.example.decoratebackservice.entity;



import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * @BelongsProject:dream_house
 * @BelongsPackage:com.example.dream_house.model
 * @Author:Uestc_Xiye
 * @CreateTime:2023-12-17 16:29:49
 */

@lombok.Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("数据库字段")
public class User {

    @ApiModelProperty(value = "用户ID", required = true, example = "1")
    private int id;

    @ApiModelProperty(value = "用户名", required = true, example = "用户名")
    private String username;

    @ApiModelProperty(value = "电话", required = true, example = "13800000000")
    private String phone;

    @ApiModelProperty(value = "密码", required = true, example = "123456")
    private String password;

    @ApiModelProperty(value = "头像文件地址", required = true, example = "path")
    private String avatar;

    @ApiModelProperty(value = "昵称", required = true, example = "niname")
    private String nickname;

    @ApiModelProperty(value = "性别", required = true, example = "男")
    private int gender;

    @ApiModelProperty(value = "昵称", required = true, example = "1")
    private int user_type;

    @ApiModelProperty(value = "角色类型", required = true, example = "2")
    private int role_id;

    @ApiModelProperty(value = "个人简介", required = true, example = "xxx")
    private String profile;

    @ApiModelProperty(value = "违禁次数", required = true, example = "3")
    private int violation_count;

    @ApiModelProperty(value = "账户状态", required = true, example = "1")
    private int status;

    @ApiModelProperty(value = "删除", required = true, example = "1")
    private int is_deleted;


}
