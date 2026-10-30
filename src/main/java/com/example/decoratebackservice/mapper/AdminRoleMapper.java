package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.Role;
import org.apache.ibatis.annotations.*;

/**
 * 管理员角色查询 Mapper：读取 sys_role（管理员角色已并入 sys_role）
 * <p>
 * 复用 Role 实体承载结果
 */
@Mapper
public interface AdminRoleMapper {

    @Results(id = "adminRoleResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "role_name", column = "role_name"),
            @Result(property = "role_code", column = "role_code"),
            @Result(property = "permissions", column = "permissions"),
            @Result(property = "remark", column = "remark"),
            @Result(property = "is_deleted", column = "is_deleted")
    })
    @Select("select * from sys_role where id = #{id}")
    Role findById(@Param("id") int id);
}
