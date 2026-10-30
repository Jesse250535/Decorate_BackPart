package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.SysAdmin;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 管理员账号 Mapper（管理员已并入 sys_user，is_admin=1 区分）
 */
@Mapper
public interface SysAdminMapper {

    @Insert("insert into sys_user (username,phone,password,avatar,nickname,gender,role_id,profile," +
            "violation_count,status,is_deleted,is_admin) values(#{username},#{phone},#{password},#{avatar},#{nickname}," +
            "#{gender},#{role_id},#{profile},#{violation_count},#{status},#{is_deleted},1)")
    int insert(SysAdmin admin);

    @Results(id = "adminResult", value = {
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
    @Select("select * from sys_user where id = #{id} and is_admin = 1")
    SysAdmin findById(@Param("id") int id);

    @ResultMap("adminResult")
    @Select("select * from sys_user where username = #{username} and is_admin = 1")
    SysAdmin findByUsername(@Param("username") String username);

    @ResultMap("adminResult")
    @Select("select * from sys_user where is_admin = 1")
    List<SysAdmin> findAll();

    @Update("update sys_user set username=#{username},phone=#{phone},password=#{password},avatar=#{avatar}," +
            "nickname=#{nickname},gender=#{gender},role_id=#{role_id},profile=#{profile}," +
            "violation_count=#{violation_count},status=#{status},is_deleted=#{is_deleted} where id=#{id} and is_admin = 1")
    int update(SysAdmin admin);

    @Delete("delete from sys_user where id = #{id} and is_admin = 1")
    int delete(@Param("id") int id);

    @Select("select * from sys_user where username = #{username} and password = #{password} and is_admin = 1")
    SysAdmin findByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
}
