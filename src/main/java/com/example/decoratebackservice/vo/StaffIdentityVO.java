package com.example.decoratebackservice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 当前登录用户的身份信息，供前端路由守卫与员工管理页初始化使用。
 * <p>
 * 与 /user/full/current 的区别：那个接口返回 sys_user 的全部字段，但拿不到
 * role_code；本 VO 专门回答「我是什么角色、能进哪个管理页」。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "当前登录用户身份信息")
public class StaffIdentityVO {

    @Schema(description = "用户ID", example = "6")
    private int user_id;

    @Schema(description = "用户名", example = "bertg")
    private String username;

    @Schema(description = "昵称", example = "王库管")
    private String nickname;

    @Schema(description = "角色ID", example = "4")
    private int role_id;

    @Schema(description = "角色标识", example = "ROLE_STOCK")
    private String role_code;

    @Schema(description = "角色名称", example = "库管人员")
    private String role_name;

    @Schema(description = "权限标识集合", example = "[\"stock:operate\",\"stock:view\"]")
    private List<String> permissions;

    @Schema(description = "是否库管人员 0否1是", example = "1")
    private int is_stock;

    @Schema(description = "是否施工人员 0否1是", example = "0")
    private int is_worker;
}
