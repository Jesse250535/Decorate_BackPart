package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * @BelongsProject:dream_house
 * @BelongsPackage:com.example.dream_house.mapper
 * @Author:Uestc_Xiye
 * @CreateTime:2023-12-17 16:36:57
 */

@Mapper
public interface UserMapper {

    /*

     * @param id
     *@param username
     * @param phone
     * @param password
     * @param avatar
     * @param nickname
     * @param gender
     * @param user_type
     * @param role_id
     * @param profile
     * @param violation_count
     * @param status
     * @param is_deleted
     *
     * @return
     */

    /*
    * id
username,phone,password,avatar,nickname,gender,user_type,role_id,profile,violation_count,status,create_time,update_time,is_deleted
* #{username},#{phone},#{password},#{avatar},#{nickname},#{gender},#{user_type},#{role_id},#{profile},#{violation_count},#{status},#{create_time},#{update_time},#{is_deleted}

    * */






    @Insert("insert into sys_user (username,phone,password,avatar,nickname,gender,user_type,role_id,profile,violation_count,status,is_deleted)" +
            " values(#{username},#{phone},#{password},#{avatar},#{nickname},#{gender},#{user_type},#{role_id},#{profile},#{violation_count},#{status},#{is_deleted})")
    int insert(@Param("username") String username,
               @Param("phone") String phone,
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


    /**
     * 信息id
     * @param id
     *
     * @return
     *
     * property属性对应Data对象中的成员名，column对应select出的字段名。
     */
    @Results({
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
    @Select("select * from sys_user where id = #{id}")
    User findById(@Param("id") int id);

    /**
     * 用Data对象来作为传参,这样语句中的#{id}、#{ip}等数据就分别对应Data对象中的id和ip等属性。
     *
     * @param user
     */
    @Update("update sys_user set username=#{username},phone=#{phone},password=#{password},avatar=#{avatar},nickname=#{nickname},gender=#{gender},user_type=#{user_type},role_id=#{role_id},profile=#{profile},violation_count=#{violation_count},status=#{status},is_deleted=#{is_deleted} where id=#{id}")
    void update(User user);

    /**
     * 删除该id对应的信息
     *
     * @param id
     */
    @Delete("delete from sys_user where id =#{id}")
    void delete(int id);

    @Select("select * from sys_user")
    List<User> findAll();
}
