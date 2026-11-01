package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.ChatConversation;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话 Mapper
 * <p>
 * 与项目其它 Mapper 保持一致：显式 @Results 映射，避免全局驼峰映射把
 * user_id / designer_id 这类下划线列丢弃；未读数用「自增 / 清零」两条 UPDATE 维护。
 */
@Mapper
public interface ChatConversationMapper {

    @Insert("insert into chat_conversation (user_id,designer_id,plan_id,last_message,last_time," +
            "user_unread,designer_unread,status) values (#{user_id},#{designer_id},#{plan_id}," +
            "#{last_message},#{last_time},#{user_unread},#{designer_unread},#{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ChatConversation conversation);

    @Results(id = "chatConvResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "user_id", column = "user_id"),
            @Result(property = "designer_id", column = "designer_id"),
            @Result(property = "plan_id", column = "plan_id"),
            @Result(property = "last_message", column = "last_message"),
            @Result(property = "last_time", column = "last_time"),
            @Result(property = "user_unread", column = "user_unread"),
            @Result(property = "designer_unread", column = "designer_unread"),
            @Result(property = "status", column = "status"),
            @Result(property = "create_time", column = "create_time"),
            @Result(property = "update_time", column = "update_time")
    })
    @Select("select id,user_id,designer_id,plan_id,last_message,last_time,user_unread,designer_unread," +
            "status,create_time,update_time from chat_conversation where id = #{id}")
    ChatConversation findById(@Param("id") long id);

    @ResultMap("chatConvResult")
    @Select("select id,user_id,designer_id,plan_id,last_message,last_time,user_unread,designer_unread," +
            "status,create_time,update_time from chat_conversation " +
            "where user_id = #{user_id} and designer_id = #{designer_id}")
    ChatConversation findByUserAndDesigner(@Param("user_id") int userId,
                                           @Param("designer_id") int designerId);

    @ResultMap("chatConvResult")
    @Select("select id,user_id,designer_id,plan_id,last_message,last_time,user_unread,designer_unread," +
            "status,create_time,update_time from chat_conversation " +
            "where user_id = #{user_id} order by last_time desc, id desc")
    List<ChatConversation> findByUser(@Param("user_id") int userId);

    @ResultMap("chatConvResult")
    @Select("select id,user_id,designer_id,plan_id,last_message,last_time,user_unread,designer_unread," +
            "status,create_time,update_time from chat_conversation " +
            "where designer_id = #{designer_id} order by last_time desc, id desc")
    List<ChatConversation> findByDesigner(@Param("designer_id") int designerId);

    @Update("update chat_conversation set last_message = #{last_message}, last_time = #{last_time} " +
            "where id = #{id}")
    int updateLast(@Param("id") long id,
                   @Param("last_message") String lastMessage,
                   @Param("last_time") LocalDateTime lastTime);

    @Update("update chat_conversation set user_unread = user_unread + 1 where id = #{id}")
    int incrUserUnread(@Param("id") long id);

    @Update("update chat_conversation set designer_unread = designer_unread + 1 where id = #{id}")
    int incrDesignerUnread(@Param("id") long id);

    @Update("update chat_conversation set user_unread = 0 where id = #{id}")
    int clearUserUnread(@Param("id") long id);

    @Update("update chat_conversation set designer_unread = 0 where id = #{id}")
    int clearDesignerUnread(@Param("id") long id);
}
