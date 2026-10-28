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

    /* ==================== 方案提供商上传 / 设计师审核 ==================== */

    /**
     * 查询某个装修方案提供商上传的全部方案
     *
     * @param create_user_id 上传人 sys_user.id
     * @return 该账号上传的方案，最新的在前
     */
    @ResultMap("planResult")
    @Select("select * from decoration_plan where create_user_id = #{create_user_id} " +
            "and is_deleted = 0 order by id desc")
    List<DecorationPlan> findByCreateUserId(@Param("create_user_id") int create_user_id);

    /**
     * 设计师审核方案：只改审核状态与审核意见，不动方案的其它字段
     *
     * @param id           方案ID
     * @param audit_status 0待审核 1通过 2驳回
     * @param audit_remark 审核意见
     * @return 受影响行数
     */
    @Update("update decoration_plan set audit_status=#{audit_status},audit_remark=#{audit_remark} where id=#{id}")
    int audit(@Param("id") int id,
              @Param("audit_status") int audit_status,
              @Param("audit_remark") String audit_remark);

    /**
     * 单独改上下架状态。
     * <p>
     * 不能用上面的 {@code update}：它是全字段无条件覆盖，传一个只带 id/status 的对象
     * 会把 plan_name、price 等 NOT NULL 列写成 null 直接报错。
     *
     * @param id     方案ID
     * @param status 0下架 1上架
     * @return 受影响行数
     */
    @Update("update decoration_plan set status=#{status} where id=#{id}")
    int updateStatus(@Param("id") int id, @Param("status") int status);
}
