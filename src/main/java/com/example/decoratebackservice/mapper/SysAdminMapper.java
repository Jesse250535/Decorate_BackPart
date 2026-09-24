package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.SysAdmin;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 管理员账号 Mapper
 */
@Mapper
public interface SysAdminMapper {

    @Insert("insert into sys_admin (username,phone,password,avatar,nickname,gender,user_type,role_id,profile," +
            "violation_count,status,is_deleted) values(#{username},#{phone},#{password},#{avatar},#{nickname}," +
            "#{gender},#{user_type},#{role_id},#{profile},#{violation_count},#{status},#{is_deleted})")
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
    @Select("select * from sys_admin where id = #{id}")
    SysAdmin findById(@Param("id") int id);

    @ResultMap("adminResult")
    @Select("select * from sys_admin where username = #{username}")
    SysAdmin findByUsername(@Param("username") String username);

    @ResultMap("adminResult")
    @Select("select * from sys_admin")
    List<SysAdmin> findAll();

    @Update("update sys_admin set username=#{username},phone=#{phone},password=#{password},avatar=#{avatar}," +
            "nickname=#{nickname},gender=#{gender},user_type=#{user_type},role_id=#{role_id},profile=#{profile}," +
            "violation_count=#{violation_count},status=#{status},is_deleted=#{is_deleted} where id=#{id}")
    int update(SysAdmin admin);

    @Delete("delete from sys_admin where id = #{id}")
    int delete(@Param("id") int id);
}
