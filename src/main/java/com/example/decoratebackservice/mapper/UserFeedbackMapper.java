package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.UserFeedback;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户意见反馈 Mapper
 */
@Mapper
public interface UserFeedbackMapper {

    @Insert("insert into user_feedback (user_id,content,contact_info,handle_status,handle_remark,is_deleted) " +
            "values(#{user_id},#{content},#{contact_info},#{handle_status},#{handle_remark},#{is_deleted})")
    int insert(UserFeedback feedback);

    @Results(id = "feedbackResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "user_id", column = "user_id"),
            @Result(property = "content", column = "content"),
            @Result(property = "contact_info", column = "contact_info"),
            @Result(property = "handle_status", column = "handle_status"),
            @Result(property = "handle_remark", column = "handle_remark"),
            @Result(property = "is_deleted", column = "is_deleted")
    })
    @Select("select * from user_feedback where id = #{id}")
    UserFeedback findById(@Param("id") int id);

    @ResultMap("feedbackResult")
    @Select("select * from user_feedback")
    List<UserFeedback> findAll();

    @Update("update user_feedback set user_id=#{user_id},content=#{content},contact_info=#{contact_info}," +
            "handle_status=#{handle_status},handle_remark=#{handle_remark},is_deleted=#{is_deleted} where id=#{id}")
    int update(UserFeedback feedback);

    @Delete("delete from user_feedback where id = #{id}")
    int delete(@Param("id") int id);
}
