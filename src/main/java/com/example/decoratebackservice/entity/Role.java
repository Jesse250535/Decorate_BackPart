package com.example.decoratebackservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色实体（sys_role）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "角色实体")
public class Role {

    @Schema(description = "主键ID", example = "1")
    private int id;

    @Schema(description = "角色名称", example = "管理员")
    private String role_name;

    @Schema(description = "角色标识", example = "ROLE_ADMIN")
    private String role_code;

    @Schema(description = "权限标识集合", example = "*")
    private String permissions;

    @Schema(description = "角色备注", example = "系统最高权限管理员")
    private String remark;

    @Schema(description = "逻辑删除 0未删1已删", example = "0")
    private int is_deleted;
}
