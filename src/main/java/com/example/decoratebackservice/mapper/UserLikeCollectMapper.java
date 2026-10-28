package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.UserLikeCollect;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户点赞收藏 Mapper
 */
@Mapper
public interface UserLikeCollectMapper {

    @Insert("insert into user_like_collect (user_id,operate_type,target_type,target_id,is_deleted) " +
            "values(#{user_id},#{operate_type},#{target_type},#{target_id},#{is_deleted})")
    int insert(UserLikeCollect likeCollect);

    @Results(id = "likeCollectResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "user_id", column = "user_id"),
            @Result(property = "operate_type", column = "operate_type"),
            @Result(property = "target_type", column = "target_type"),
            @Result(property = "target_id", column = "target_id"),
            @Result(property = "is_deleted", column = "is_deleted"),
            @Result(property = "create_time", column = "create_time")
    })
    @Select("select * from user_like_collect where id = #{id}")
    UserLikeCollect findById(@Param("id") int id);

    @ResultMap("likeCollectResult")
    @Select("select * from user_like_collect")
    List<UserLikeCollect> findAll();

    @Update("update user_like_collect set user_id=#{user_id},operate_type=#{operate_type}," +
            "target_type=#{target_type},target_id=#{target_id},is_deleted=#{is_deleted} where id=#{id}")
    int update(UserLikeCollect likeCollect);

    @Delete("delete from user_like_collect where id = #{id}")
    int delete(@Param("id") int id);

    /* ==================== 以下为「我的收藏」相关新增 SQL ==================== */

    /**
     * 查询某个用户的有效收藏记录（排除逻辑删除），按收藏时间倒序
     *
     * @param user_id      用户ID（sys_user.id）
     * @param operate_type 操作类型 1点赞 2收藏
     * @return 该用户的收藏记录
     */
    @ResultMap("likeCollectResult")
    @Select("select * from user_like_collect where user_id = #{user_id} " +
            "and operate_type = #{operate_type} and is_deleted = 0 " +
            "order by create_time desc, id desc")
    List<UserLikeCollect> findByUserAndOperate(@Param("user_id") int user_id,
                                               @Param("operate_type") int operate_type);

    /**
     * 收藏（写入）：表上有 uk_user_operate 唯一索引，
     * 已存在时只把 is_deleted 复位为 0，避免重复收藏插入失败
     *
     * @param user_id      用户ID
     * @param operate_type 操作类型
     * @param target_type  对象类型 1装修方案 2装修材料 3评论
     * @param target_id    对象ID
     * @return 受影响行数
     */
    @Insert("insert into user_like_collect (user_id,operate_type,target_type,target_id,is_deleted) " +
            "values(#{user_id},#{operate_type},#{target_type},#{target_id},0) " +
            "on duplicate key update is_deleted = 0")
    int upsertCollect(@Param("user_id") int user_id,
                      @Param("operate_type") int operate_type,
                      @Param("target_type") int target_type,
                      @Param("target_id") int target_id);

    /**
     * 取消收藏：逻辑删除，保留记录便于统计
     *
     * @param user_id      用户ID
     * @param operate_type 操作类型
     * @param target_type  对象类型
     * @param target_id    对象ID
     * @return 受影响行数，0 表示本来就没收藏过
     */
    @Update("update user_like_collect set is_deleted = 1 " +
            "where user_id = #{user_id} and operate_type = #{operate_type} " +
            "and target_type = #{target_type} and target_id = #{target_id} and is_deleted = 0")
    int cancelCollect(@Param("user_id") int user_id,
                      @Param("operate_type") int operate_type,
                      @Param("target_type") int target_type,
                      @Param("target_id") int target_id);

    /**
     * 判断某个用户是否已收藏该对象
     *
     * @param user_id      用户ID
     * @param operate_type 操作类型
     * @param target_type  对象类型
     * @param target_id    对象ID
     * @return 有效记录数（0 或 1）
     */
    @Select("select count(*) from user_like_collect where user_id = #{user_id} " +
            "and operate_type = #{operate_type} and target_type = #{target_type} " +
            "and target_id = #{target_id} and is_deleted = 0")
    int countCollected(@Param("user_id") int user_id,
                       @Param("operate_type") int operate_type,
                       @Param("target_type") int target_type,
                       @Param("target_id") int target_id);

    /* ==================== 以下为评论点赞相关 SQL（operate_type=1，target_type=3） ==================== */

    /**
     * 点赞评论：唯一索引 uk_user_operate 保证一个用户对同一评论只有一条记录，
     * 重复点赞只把 is_deleted 复位为 0
     *
     * @param user_id   当前登录用户ID
     * @param target_id 评论ID
     * @return 受影响行数
     */
    @Insert("insert into user_like_collect (user_id,operate_type,target_type,target_id,is_deleted) " +
            "values(#{user_id},1,3,#{target_id},0) " +
            "on duplicate key update is_deleted = 0")
    int upsertCommentLike(@Param("user_id") int user_id, @Param("target_id") int target_id);

    /**
     * 取消评论点赞（逻辑删除）
     *
     * @param user_id   当前登录用户ID
     * @param target_id 评论ID
     * @return 受影响行数，0 表示本来就没点赞
     */
    @Update("update user_like_collect set is_deleted = 1 where user_id = #{user_id} " +
            "and operate_type = 1 and target_type = 3 and target_id = #{target_id} and is_deleted = 0")
    int cancelCommentLike(@Param("user_id") int user_id, @Param("target_id") int target_id);

    /**
     * 判断用户是否已点赞该评论
     *
     * @return 有效记录数（0 或 1）
     */
    @Select("select count(*) from user_like_collect where user_id = #{user_id} " +
            "and operate_type = 1 and target_type = 3 and target_id = #{target_id} and is_deleted = 0")
    int countCommentLiked(@Param("user_id") int user_id, @Param("target_id") int target_id);

    /**
     * 查询用户点赞过的全部评论ID，用于一次性标记列表里的点赞状态，避免逐条查询
     *
     * @param user_id 当前登录用户ID
     * @return 评论ID列表
     */
    @Select("select target_id from user_like_collect where user_id = #{user_id} " +
            "and operate_type = 1 and target_type = 3 and is_deleted = 0")
    List<Integer> findLikedCommentIds(@Param("user_id") int user_id);
}
