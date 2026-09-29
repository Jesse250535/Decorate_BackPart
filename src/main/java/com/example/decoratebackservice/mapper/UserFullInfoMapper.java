package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.UserFullInfo;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户完整信息查询 Mapper：读取 sys_user 全部列（含 email、create_time、update_time）
 * <p>
 * 新增独立文件，未改动既有 UserMapper，遵循非侵入式约定
 */
@Mapper
public interface UserFullInfoMapper {

    @Results(id = "userFullResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "email", column = "email"),
            @Result(property = "password", column = "password"),
            @Result(property = "avatar", column = "avatar"),
            @Result(property = "nickname", column = "nickname"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "user_type", column = "user_type"),
            @Result(property = "role_id", column = "role_id"),
            @Result(property = "profile", column = "profile"),
            @Result(property = "violation_count", column = "violation_count"),
            @Result(property = "status", column = "status"),
            @Result(property = "create_time", column = "create_time"),
            @Result(property = "update_time", column = "update_time"),
            @Result(property = "is_deleted", column = "is_deleted")
    })
    @Select("select * from sys_user where id = #{id}")
    UserFullInfo findById(@Param("id") int id);

    @ResultMap("userFullResult")
    @Select("select * from sys_user")
    List<UserFullInfo> findAll();
}
