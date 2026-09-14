package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.SysVisitStat;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 系统访问统计 Mapper
 */
@Mapper
public interface SysVisitStatMapper {

    @Insert("insert into sys_visit_stat (stat_date,pv,uv,new_user_count,create_plan_count,create_material_count," +
            "finish_order_count) values(#{stat_date},#{pv},#{uv},#{new_user_count},#{create_plan_count}," +
            "#{create_material_count},#{finish_order_count})")
    int insert(SysVisitStat stat);

    @Results(id = "visitStatResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "stat_date", column = "stat_date"),
            @Result(property = "pv", column = "pv"),
            @Result(property = "uv", column = "uv"),
            @Result(property = "new_user_count", column = "new_user_count"),
            @Result(property = "create_plan_count", column = "create_plan_count"),
            @Result(property = "create_material_count", column = "create_material_count"),
            @Result(property = "finish_order_count", column = "finish_order_count")
    })
    @Select("select * from sys_visit_stat where id = #{id}")
    SysVisitStat findById(@Param("id") int id);

    @ResultMap("visitStatResult")
    @Select("select * from sys_visit_stat")
    List<SysVisitStat> findAll();

    @Update("update sys_visit_stat set stat_date=#{stat_date},pv=#{pv},uv=#{uv},new_user_count=#{new_user_count}," +
            "create_plan_count=#{create_plan_count},create_material_count=#{create_material_count}," +
            "finish_order_count=#{finish_order_count} where id=#{id}")
    int update(SysVisitStat stat);

    @Delete("delete from sys_visit_stat where id = #{id}")
    int delete(@Param("id") int id);
}
