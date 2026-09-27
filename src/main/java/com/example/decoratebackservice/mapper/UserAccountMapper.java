package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户账号登录查询 Mapper：支持按邮箱、昵称检索前台用户
 * <p>
 * 独立于既有 UserMapper，避免改动原有代码
 */
@Mapper
public interface UserAccountMapper {

    @Results(id = "userAccountResult", value = {
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
    @Select("select * from sys_user where email = #{email} and is_deleted = 0")
    List<User> findByEmail(@Param("email") String email);

    @ResultMap("userAccountResult")
    @Select("select * from sys_user where nickname = #{nickname} and is_deleted = 0")
    List<User> findByNickname(@Param("nickname") String nickname);
}
