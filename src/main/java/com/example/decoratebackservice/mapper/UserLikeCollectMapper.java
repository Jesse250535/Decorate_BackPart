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
            @Result(property = "is_deleted", column = "is_deleted")
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
}
