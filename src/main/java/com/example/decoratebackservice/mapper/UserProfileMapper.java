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
 * 每个方法只更新一个列。既有的 UserMapper.update(User) 是「全字段覆盖」写法
 * （update sys_user set username=..., phone=..., password=...），只传部分字段调用会把
 * 未传字段写成 null/0，因此单字段修改必须走本 Mapper。
 * <p>
 * update_time 列在建表时已声明 ON UPDATE CURRENT_TIMESTAMP，会自动刷新，无需手工赋值。
 */
@Mapper
public interface UserProfileMapper {

    /**
     * 判断用户是否存在（排除逻辑删除）
     */
    @Select("select count(1) from sys_user where id = #{id} and is_deleted = 0")
    int countById(@Param("id") int id);

    /**
     * 手机号占用检查。
     * <p>
     * 这里刻意不加 is_deleted = 0 条件：phone 上的唯一索引 uk_phone 覆盖全表所有行，
     * 若某条逻辑删除记录占着该号码，插入/更新同样会撞唯一索引，判定范围必须与索引一致。
     */
    @Select("select count(1) from sys_user where phone = #{phone} and id <> #{id}")
    int countByPhoneExcludeId(@Param("phone") String phone, @Param("id") int id);

    @Update("update sys_user set phone = #{phone} where id = #{id} and is_deleted = 0")
    int updatePhone(@Param("id") int id, @Param("phone") String phone);

    @Update("update sys_user set email = #{email} where id = #{id} and is_deleted = 0")
    int updateEmail(@Param("id") int id, @Param("email") String email);

    @Update("update sys_user set nickname = #{nickname} where id = #{id} and is_deleted = 0")
    int updateNickname(@Param("id") int id, @Param("nickname") String nickname);

    @Update("update sys_user set gender = #{gender} where id = #{id} and is_deleted = 0")
    int updateGender(@Param("id") int id, @Param("gender") int gender);

    @Update("update sys_user set user_type = #{userType} where id = #{id} and is_deleted = 0")
    int updateUserType(@Param("id") int id, @Param("userType") int userType);
}
