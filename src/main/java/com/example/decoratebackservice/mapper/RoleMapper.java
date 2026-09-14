package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 角色 Mapper
 */
@Mapper
public interface RoleMapper {

    @Insert("insert into sys_role (role_name,role_code,permissions,remark,is_deleted)" +
            " values(#{role_name},#{role_code},#{permissions},#{remark},#{is_deleted})")
    int insert(Role role);

    @Results(id = "roleResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "role_name", column = "role_name"),
            @Result(property = "role_code", column = "role_code"),
            @Result(property = "permissions", column = "permissions"),
            @Result(property = "remark", column = "remark"),
            @Result(property = "is_deleted", column = "is_deleted")
    })
    @Select("select * from sys_role where id = #{id}")
    Role findById(@Param("id") int id);

    @ResultMap("roleResult")
    @Select("select * from sys_role")
    List<Role> findAll();

    @Update("update sys_role set role_name=#{role_name},role_code=#{role_code},permissions=#{permissions}," +
            "remark=#{remark},is_deleted=#{is_deleted} where id=#{id}")
    int update(Role role);

    @Delete("delete from sys_role where id = #{id}")
    int delete(@Param("id") int id);
}
