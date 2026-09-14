package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.DecorationPlan;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 装修方案 Mapper
 */
@Mapper
public interface DecorationPlanMapper {

    @Insert("insert into decoration_plan (plan_name,cover_image,detail_images,price,area_min,area_max,style," +
            "description,category_id,create_user_id,audit_status,audit_remark,sales_count,view_count,collect_count," +
            "status,is_deleted) values(#{plan_name},#{cover_image},#{detail_images},#{price},#{area_min},#{area_max}," +
            "#{style},#{description},#{category_id},#{create_user_id},#{audit_status},#{audit_remark},#{sales_count}," +
            "#{view_count},#{collect_count},#{status},#{is_deleted})")
    int insert(DecorationPlan plan);

    @Results(id = "planResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "plan_name", column = "plan_name"),
            @Result(property = "cover_image", column = "cover_image"),
            @Result(property = "detail_images", column = "detail_images"),
            @Result(property = "price", column = "price"),
            @Result(property = "area_min", column = "area_min"),
            @Result(property = "area_max", column = "area_max"),
            @Result(property = "style", column = "style"),
            @Result(property = "description", column = "description"),
            @Result(property = "category_id", column = "category_id"),
            @Result(property = "create_user_id", column = "create_user_id"),
            @Result(property = "audit_status", column = "audit_status"),
            @Result(property = "audit_remark", column = "audit_remark"),
            @Result(property = "sales_count", column = "sales_count"),
            @Result(property = "view_count", column = "view_count"),
            @Result(property = "collect_count", column = "collect_count"),
            @Result(property = "status", column = "status"),
            @Result(property = "is_deleted", column = "is_deleted")
    })
    @Select("select * from decoration_plan where id = #{id}")
    DecorationPlan findById(@Param("id") int id);

    @ResultMap("planResult")
    @Select("select * from decoration_plan")
    List<DecorationPlan> findAll();

    @Update("update decoration_plan set plan_name=#{plan_name},cover_image=#{cover_image}," +
            "detail_images=#{detail_images},price=#{price},area_min=#{area_min},area_max=#{area_max}," +
            "style=#{style},description=#{description},category_id=#{category_id},create_user_id=#{create_user_id}," +
            "audit_status=#{audit_status},audit_remark=#{audit_remark},sales_count=#{sales_count}," +
            "view_count=#{view_count},collect_count=#{collect_count},status=#{status},is_deleted=#{is_deleted} " +
            "where id=#{id}")
    int update(DecorationPlan plan);

    @Delete("delete from decoration_plan where id = #{id}")
    int delete(@Param("id") int id);
}
