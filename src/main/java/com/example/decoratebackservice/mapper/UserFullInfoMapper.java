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

    @ResultMap("userFullResult")
    @Select("select * from sys_user where username = #{username}")
    UserFullInfo findByUsername(@Param("username") String username);

    /**
     * 按用户名更新「个人资料」页允许前台自行修改的列。
     * <p>
     * 仅包含 nickname / phone / email / gender / user_type / profile 六列。
     * username、password、role_id、violation_count、status、is_deleted 一律不允许前台改动。
     * update_time 列在 DDL 中已声明 ON UPDATE CURRENT_TIMESTAMP，无需显式赋值。
     * <p>
     * 既有 UserMapper.update 的 SQL 不含 email 列，故此处独立实现，不改动既有 Mapper。
     *
     * @param user 必须携带 username（作为定位条件），其余为待更新值
     * @return 受影响行数
     */
    @Update("update sys_user set nickname = #{nickname}, phone = #{phone}, email = #{email}, " +
            "gender = #{gender}, user_type = #{user_type}, profile = #{profile}, " +
            // avatar 现在保存的是上传接口返回的 URL（不再是 base64）：
            // 用 COALESCE 保证调用方没传 avatar 时保留原值，不会误清空已上传的头像
            "avatar = COALESCE(#{avatar}, avatar) " +
            "where username = #{username}")
    int updateByUsername(UserFullInfo user);
}
