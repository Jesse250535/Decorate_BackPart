package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.ChatMessage;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 聊天消息 Mapper
 * <p>
 * 增量拉取用 id > after_id：前端记录已拿到的最大 id，轮询时只取新消息，
 * 既不重复渲染，也避免每次全量查询。
 */
@Mapper
public interface ChatMessageMapper {

    @Insert("insert into chat_message (conversation_id,sender_id,sender_role,msg_type,content," +
            "image_url,target_id,is_read,is_deleted) values (#{conversation_id},#{sender_id}," +
            "#{sender_role},#{msg_type},#{content},#{image_url},#{target_id},#{is_read},#{is_deleted})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ChatMessage message);

    @Results(id = "chatMsgResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "conversation_id", column = "conversation_id"),
            @Result(property = "sender_id", column = "sender_id"),
            @Result(property = "sender_role", column = "sender_role"),
            @Result(property = "msg_type", column = "msg_type"),
            @Result(property = "content", column = "content"),
            @Result(property = "image_url", column = "image_url"),
            @Result(property = "target_id", column = "target_id"),
            @Result(property = "is_read", column = "is_read"),
            @Result(property = "create_time", column = "create_time"),
            @Result(property = "is_deleted", column = "is_deleted")
    })
    @Select("select id,conversation_id,sender_id,sender_role,msg_type,content,image_url,target_id," +
            "is_read,create_time,is_deleted from chat_message where id = #{id}")
    ChatMessage findById(@Param("id") long id);

    @ResultMap("chatMsgResult")
    @Select("select id,conversation_id,sender_id,sender_role,msg_type,content,image_url,target_id," +
            "is_read,create_time,is_deleted from chat_message " +
            "where conversation_id = #{conversation_id} and is_deleted = 0 and id > #{after_id} " +
            "order by id asc limit 300")
    List<ChatMessage> findByConversation(@Param("conversation_id") long conversationId,
                                         @Param("after_id") long afterId);

    /**
     * 把「对方发来的」未读消息标记为已读
     */
    @Update("update chat_message set is_read = 1 where conversation_id = #{conversation_id} " +
            "and sender_role <> #{reader_role} and is_read = 0")
    int markRead(@Param("conversation_id") long conversationId,
                 @Param("reader_role") int readerRole);
}
