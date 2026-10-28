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
    @Options(useGeneratedKeys = true, keyProperty = "id")
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

    /* ==================== 以下为详情页评论区新增 SQL ==================== */

    /**
     * 查询某个方案 / 材料下的全部未删除评论（含回复，parent_id 区分），
     * 由业务层组装成两级结构
     *
     * @param target_type 1装修方案 2装修材料
     * @param target_id   方案 / 材料ID
     * @return 评论列表，按发布时间倒序
     */
    @ResultMap("commentResult")
    @Select("select * from decoration_comment where target_type = #{target_type} " +
            "and target_id = #{target_id} and status = 0 and is_deleted = 0 " +
            "order by create_time desc, id desc")
    List<DecorationComment> findByTarget(@Param("target_type") int target_type,
                                         @Param("target_id") int target_id);

    /**
     * 统计某个方案 / 材料下的有效评论数
     */
    @Select("select count(*) from decoration_comment where target_type = #{target_type} " +
            "and target_id = #{target_id} and status = 0 and is_deleted = 0")
    int countByTarget(@Param("target_type") int target_type, @Param("target_id") int target_id);

    /**
     * 判断评论是否存在且可展示（未被删除、未被判定违规）
     */
    @Select("select count(*) from decoration_comment where id = #{id} and status = 0 and is_deleted = 0")
    int countValidById(@Param("id") int id);

    /**
     * 点赞数增减。delta 为 1（点赞）或 -1（取消点赞），
     * 条件里带 like_count + delta >= 0，避免并发下把计数减成负数
     *
     * @param id    评论ID
     * @param delta 增减量
     * @return 受影响行数
     */
    @Update("update decoration_comment set like_count = like_count + #{delta} " +
            "where id = #{id} and like_count + #{delta} >= 0")
    int addLikeCount(@Param("id") int id, @Param("delta") int delta);
}
