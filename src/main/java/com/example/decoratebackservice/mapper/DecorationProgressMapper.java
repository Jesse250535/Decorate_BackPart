package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.DecorationProgress;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 装修进度跟踪 Mapper
 */
@Mapper
public interface DecorationProgressMapper {

    @Insert("insert into decoration_progress (order_id,progress_status,status_desc,progress_images,report_user_id," +
            "is_deleted) values(#{order_id},#{progress_status},#{status_desc},#{progress_images},#{report_user_id}," +
            "#{is_deleted})")
    int insert(DecorationProgress progress);

    @Results(id = "progressResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "order_id", column = "order_id"),
            @Result(property = "progress_status", column = "progress_status"),
            @Result(property = "status_desc", column = "status_desc"),
            @Result(property = "progress_images", column = "progress_images"),
            @Result(property = "report_user_id", column = "report_user_id"),
            @Result(property = "is_deleted", column = "is_deleted")
    })
    @Select("select * from decoration_progress where id = #{id}")
    DecorationProgress findById(@Param("id") int id);

    @ResultMap("progressResult")
    @Select("select * from decoration_progress")
    List<DecorationProgress> findAll();

    @Update("update decoration_progress set order_id=#{order_id},progress_status=#{progress_status}," +
            "status_desc=#{status_desc},progress_images=#{progress_images},report_user_id=#{report_user_id}," +
            "is_deleted=#{is_deleted} where id=#{id}")
    int update(DecorationProgress progress);

    @Delete("delete from decoration_progress where id = #{id}")
    int delete(@Param("id") int id);
}
