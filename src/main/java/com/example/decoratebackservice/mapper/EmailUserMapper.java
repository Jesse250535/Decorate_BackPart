package com.example.decoratebackservice.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 邮箱注册专用 Mapper：独立于既有 UserMapper，避免改动原有代码
 * <p>
 * 既有 UserMapper.insert 不含 email 列，这里新增带 email 的写入与去重查询
 */
@Mapper
public interface EmailUserMapper {

    /**
     * 统计用户名占用情况，用于注册前唯一性校验
     */
    @Select("select count(1) from sys_user where username = #{username}")
    int countByUsername(@Param("username") String username);

    /**
     * 统计有效邮箱占用情况，用于注册前唯一性校验
     */
    @Select("select count(1) from sys_user where email = #{email} and is_deleted = 0")
    int countByEmail(@Param("email") String email);

    /**
     * 写入前台用户（含 email 列），字段与既有注册保持一致，仅额外写入 email
     */
    @Insert("insert into sys_user (username,phone,email,password,avatar,nickname,gender,user_type,role_id,profile,violation_count,status,is_deleted)"
            + " values(#{username},#{phone},#{email},#{password},#{avatar},#{nickname},#{gender},#{user_type},#{role_id},#{profile},#{violation_count},#{status},#{is_deleted})")
    int insert(@Param("username") String username,
               @Param("phone") String phone,
               @Param("email") String email,
               @Param("password") String password,
               @Param("avatar") String avatar,
               @Param("nickname") String nickname,
               @Param("gender") String gender,
               @Param("user_type") String user_type,
               @Param("role_id") int role_id,
               @Param("profile") String profile,
               @Param("violation_count") int violation_count,
               @Param("status") String status,
               @Param("is_deleted") int is_deleted);
}
