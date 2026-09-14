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
}
