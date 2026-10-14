package com.example.decoratebackservice.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户资料单字段修改 Mapper：手机号、邮箱、昵称、性别、用户类型
 * <p>
 * 新增独立文件，未改动既有 UserMapper / UserFullInfoMapper，遵循非侵入式约定。
 * <p>
 * 所有语句一律以 username 定位目标行。username 由服务端从会话中解析得到，接口不接收该参数，
 * 前端无法指定改谁，因此天然只能修改当前登录用户自己的资料，不存在按 id 传参改他人数据的越权面。
 * <p>
 * 每个方法只更新一个列。既有的 UserMapper.update(User) 是「全字段覆盖」写法
 * （update sys_user set username=..., phone=..., password=...），只传部分字段调用会把
 * 未传字段写成 null/0，因此单字段修改必须走本 Mapper。
 * <p>
 * update_time 列在建表时已声明 ON UPDATE CURRENT_TIMESTAMP，会自动刷新，无需手工赋值。
 */
@Mapper
public interface UserProfileMapper {

    /**
     * 判断当前登录用户是否存在（排除逻辑删除）。
     * <p>
     * 不能用 update 的返回行数代替本检查：MySQL 默认未开启 CLIENT_FOUND_ROWS，
     * 提交与原值相同的字段时返回 0 行，会被误判成「用户不存在」。
     */
    @Select("select count(1) from sys_user where username = #{username} and is_deleted = 0")
    int countByUsername(@Param("username") String username);

    /**
     * 手机号占用检查：排除自己。
     * <p>
     * 刻意不加 is_deleted = 0 条件：phone 上的唯一索引 uk_phone 覆盖全表所有行，
     * 若某条逻辑删除记录占着该号码，更新同样会撞唯一索引，判定范围必须与索引一致。
     */
    @Select("select count(1) from sys_user where phone = #{phone} and username <> #{username}")
    int countByPhoneExcludeUsername(@Param("phone") String phone, @Param("username") String username);

    @Update("update sys_user set phone = #{phone} where username = #{username} and is_deleted = 0")
    int updatePhone(@Param("username") String username, @Param("phone") String phone);

    @Update("update sys_user set email = #{email} where username = #{username} and is_deleted = 0")
    int updateEmail(@Param("username") String username, @Param("email") String email);

    @Update("update sys_user set nickname = #{nickname} where username = #{username} and is_deleted = 0")
    int updateNickname(@Param("username") String username, @Param("nickname") String nickname);

    @Update("update sys_user set gender = #{gender} where username = #{username} and is_deleted = 0")
    int updateGender(@Param("username") String username, @Param("gender") int gender);

    @Update("update sys_user set user_type = #{userType} where username = #{username} and is_deleted = 0")
    int updateUserType(@Param("username") String username, @Param("userType") int userType);
}
