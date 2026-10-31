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
    @Select("select * from sys_user where id = #{id} and is_admin = 0")
    User findById(@Param("id") int id);

    @Select("select * from sys_user where username = #{username} and is_admin = 0")
    User findByUsername(@Param("username") String username);

    /**
     * 按用户名查询用户，供员工身份判定使用。
     * <p>
     * 为什么不复用上面的 findByUsername：它用 select * + 自动映射，
     * 而全局开启了驼峰映射，像 role_id 这种带下划线的列会被映射到不存在的
     * roleId 属性上并静默丢弃，导致 role_id 恒为 0。这里显式声明映射绕开该问题，
     * 同时不改动原有方法，避免影响既有调用方。
     *
     * @param username 用户名
     * @return 用户信息（含 role_id）；不存在返回 null
     */
    @Results(id = "staffUserResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "nickname", column = "nickname"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "user_type", column = "user_type"),
            @Result(property = "user_type_chosen", column = "user_type_chosen"),
            @Result(property = "role_id", column = "role_id"),
            @Result(property = "status", column = "status"),
            @Result(property = "is_deleted", column = "is_deleted")
    })
    @Select("select id, username, nickname, phone, user_type, user_type_chosen, role_id, status, is_deleted " +
            "from sys_user where username = #{username} and is_admin = 0")
    User findByUsernameForStaff(@Param("username") String username);

    /**
     * 登录认证专用：按用户名查出密码 + role_id（显式映射，避免驼峰映射丢掉带下划线的列）。
     * <p>
     * 不能用 findByUsername：它的 select * 自动映射会把 role_id 丢掉，
     * 导致登录后装配不出 ROLE_MANAGER / ROLE_DESIGNER 等角色权限，
     * 项目经理、设计师调用需要角色的接口全部 403。
     *
     * @param username 用户名
     * @return 用户信息（含 password 与 role_id）；不存在返回 null
     */
    @Results(id = "authUserResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "password", column = "password"),
            @Result(property = "nickname", column = "nickname"),
            @Result(property = "role_id", column = "role_id"),
            @Result(property = "status", column = "status"),
            @Result(property = "is_deleted", column = "is_deleted")
    })
    @Select("select * from sys_user where username = #{username} and is_admin = 0")
    User findByUsernameForAuth(@Param("username") String username);

    /**
     * 首次登录弹窗选定身份类型后回写：user_type 记下所选类型，chosen 置 1 表示不再弹窗
     *
     * @param username  用户名
     * @param user_type 用户身份类型 1客户 2装修爱好者 3购房者 4家具爱好者 5其他
     * @return 受影响行数
     */
    @Update("update sys_user set user_type = #{user_type}, user_type_chosen = 1 where username = #{username} and is_admin = 0")
    int updateUserType(@Param("username") String username, @Param("user_type") int user_type);

    /**
     * 只标记「用户类型已确认」，不改 user_type。
     * <p>
     * 库管 / 施工人员注册时用：岗位身份已由角色说明，不需要首次登录弹窗。
     *
     * @param username 用户名
     * @return 受影响行数
     */
    @Update("update sys_user set user_type_chosen = 1 where username = #{username} and is_admin = 0")
    int markUserTypeChosen(@Param("username") String username);

    /**
     * 用Data对象来作为传参,这样语句中的#{id}、#{ip}等数据就分别对应Data对象中的id和ip等属性。
     *
     * @param user
     */
    @Update("update sys_user set username=#{username},phone=#{phone},password=#{password},avatar=#{avatar},nickname=#{nickname},gender=#{gender},user_type=#{user_type},role_id=#{role_id},profile=#{profile},violation_count=#{violation_count},status=#{status},is_deleted=#{is_deleted} where id=#{id} and is_admin = 0")
    void update(User user);

    /**
     * 删除该id对应的信息
     *
     * @param id
     */
    @Delete("delete from sys_user where id =#{id} and is_admin = 0")
    void delete(int id);

    @Select("select * from sys_user where is_admin = 0")
    List<User> findAll();

    @Delete("delete from sys_user where username = #{username} and is_admin = 0")
    void deleteByUsername(@Param("username") String username);

    @Select("select username from sys_user where id = #{id} and is_admin = 0")
    String findUsernameById(@Param("id") int id);

}
