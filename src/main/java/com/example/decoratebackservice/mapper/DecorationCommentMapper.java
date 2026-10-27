package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.DecorationComment;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 评论 Mapper
 */
@Mapper
public interface DecorationCommentMapper {

    @Insert("insert into decoration_comment (user_id,target_type,target_id,parent_id,content,images,like_count," +
            "status,is_deleted) values(#{user_id},#{target_type},#{target_id},#{parent_id},#{content},#{images}," +
            "#{like_count},#{status},#{is_deleted})")
    int insert(DecorationComment comment);

    @Results(id = "commentResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "user_id", column = "user_id"),
            @Result(property = "target_type", column = "target_type"),
            @Result(property = "target_id", column = "target_id"),
            @Result(property = "parent_id", column = "parent_id"),
            @Result(property = "content", column = "content"),
            @Result(property = "images", column = "images"),
            @Result(property = "like_count", column = "like_count"),
            @Result(property = "status", column = "status"),
            @Result(property = "create_time", column = "create_time"),
            @Result(property = "is_deleted", column = "is_deleted")
    })
    @Select("select * from decoration_comment where id = #{id}")
    DecorationComment findById(@Param("id") int id);

    @ResultMap("commentResult")
    @Select("select * from decoration_comment")
    List<DecorationComment> findAll();

    @Update("update decoration_comment set user_id=#{user_id},target_type=#{target_type},target_id=#{target_id}," +
            "parent_id=#{parent_id},content=#{content},images=#{images},like_count=#{like_count},status=#{status}," +
            "is_deleted=#{is_deleted} where id=#{id}")
    int update(DecorationComment comment);

    @Delete("delete from decoration_comment where id = #{id}")
    int delete(@Param("id") int id);

    /**
     * 查询某个用户自己发布的评论（排除逻辑删除），按发布时间倒序
     *
     * @param user_id 发布用户ID（sys_user.id）
     * @return 该用户的评论列表
     */
    @ResultMap("commentResult")
    @Select("select * from decoration_comment where user_id = #{user_id} and is_deleted = 0 " +
            "order by create_time desc, id desc")
    List<DecorationComment> findByUserId(@Param("user_id") int user_id);

    /**
     * 修改「自己的」评论：只允许改内容与图片，user_id 一并作为条件防止越权改别人的评论
     *
     * @param id      评论主键
     * @param user_id 当前登录用户ID
     * @param content 新内容
     * @param images  新图片（逗号分隔，可为 null）
     * @return 受影响行数，0 表示评论不存在 / 不属于该用户 / 已被删除
     */
    @Update("update decoration_comment set content = #{content}, images = #{images} " +
            "where id = #{id} and user_id = #{user_id} and is_deleted = 0")
    int updateContentByIdAndUser(@Param("id") int id,
                                 @Param("user_id") int user_id,
                                 @Param("content") String content,
                                 @Param("images") String images);

    /**
     * 删除「自己的」评论：走逻辑删除（is_deleted=1），保留数据可追溯
     *
     * @param id      评论主键
     * @param user_id 当前登录用户ID
     * @return 受影响行数，0 表示评论不存在 / 不属于该用户 / 已被删除
     */
    @Update("update decoration_comment set is_deleted = 1 " +
            "where id = #{id} and user_id = #{user_id} and is_deleted = 0")
    int softDeleteByIdAndUser(@Param("id") int id, @Param("user_id") int user_id);
}
