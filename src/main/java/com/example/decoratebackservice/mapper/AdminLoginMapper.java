package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.SysAdmin;
import org.apache.ibatis.annotations.*;

/**
 * 管理员登录查询 Mapper：支持按用户名 / 昵称 / 手机号 读取管理员账号（sys_user 中 is_admin=1）
 * <p>
 * 新增独立文件，未改动既有 SysAdminMapper，遵循非侵入式约定
 */
@Mapper
public interface AdminLoginMapper {

    @Results(id = "adminLoginResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "password", column = "password"),
            @Result(property = "avatar", column = "avatar"),
            @Result(property = "nickname", column = "nickname"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "user_type", column = "user_type"),
            @Result(property = "role_id", column = "role_id"),
            @Result(property = "profile", column = "profile"),
            @Result(property = "violation_count", column = "violation_count"),
            @Result(property = "status", column = "status"),
            @Result(property = "is_deleted", column = "is_deleted")
    })
    @Select("select * from sys_user where username = #{account} and is_admin = 1")
    SysAdmin findByUsername(@Param("account") String account);

    @ResultMap("adminLoginResult")
    @Select("select * from sys_user where nickname = #{account} and is_admin = 1")
    SysAdmin findByNickname(@Param("account") String account);

    @ResultMap("adminLoginResult")
    @Select("select * from sys_user where phone = #{account} and is_admin = 1")
    SysAdmin findByPhone(@Param("account") String account);
}
