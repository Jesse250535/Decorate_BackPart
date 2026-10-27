package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.DecorationProgress;
import com.example.decoratebackservice.vo.OrderProgressVO;
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
            @Result(property = "create_time", column = "create_time"),
            @Result(property = "is_deleted", column = "is_deleted")
    })
    @Select("select * from decoration_progress where id = #{id}")
    DecorationProgress findById(@Param("id") int id);

    @ResultMap("progressResult")
    @Select("select * from decoration_progress")
    List<DecorationProgress> findAll();

    /**
     * 查询某个施工人员自己上报过的全部进度记录。
     * <p>
     * 施工人员只能看和改自己上报的数据，因此必须按 report_user_id 过滤，
     * 不能直接复用无条件的 findAll。
     *
     * @param report_user_id 上报人ID（sys_user.id）
     * @return 该人上报的记录，最新的在前
     */
    @ResultMap("progressResult")
    @Select("select * from decoration_progress where report_user_id = #{report_user_id} " +
            "and is_deleted = 0 order by id desc")
    List<DecorationProgress> findByReportUserId(@Param("report_user_id") int report_user_id);

    @Update("update decoration_progress set order_id=#{order_id},progress_status=#{progress_status}," +
            "status_desc=#{status_desc},progress_images=#{progress_images},report_user_id=#{report_user_id}," +
            "is_deleted=#{is_deleted} where id=#{id}")
    int update(DecorationProgress progress);

    @Delete("delete from decoration_progress where id = #{id}")
    int delete(@Param("id") int id);

    /**
     * 查询某个用户的全部订单，并带出每个订单最新的一条装修进度
     * <p>
     * 用相关子查询取 max(id)，保证一个订单只返回一行（最新上报记录）。
     * 订单状态是否允许查看由业务层判定，SQL 不做过滤，便于前端区分「不可查看」的原因。
     *
     * @param user_id 用户ID（sys_user.id）
     * @return 订单 + 最新进度（无进度时进度字段为 null）
     */
    @Results(id = "orderProgressResult", value = {
            @Result(property = "order_id", column = "order_id"),
            @Result(property = "order_no", column = "order_no"),
            @Result(property = "order_type", column = "order_type"),
            @Result(property = "target_id", column = "target_id"),
            @Result(property = "order_status", column = "order_status"),
            @Result(property = "progress_id", column = "progress_id"),
            @Result(property = "progress_status", column = "progress_status"),
            @Result(property = "status_desc", column = "status_desc"),
            @Result(property = "progress_images", column = "progress_images"),
            @Result(property = "report_time", column = "report_time")
    })
    @Select("select o.id as order_id, o.order_no as order_no, o.order_type as order_type, " +
            "o.target_id as target_id, o.order_status as order_status, " +
            "p.id as progress_id, p.progress_status as progress_status, p.status_desc as status_desc, " +
            "p.progress_images as progress_images, p.create_time as report_time " +
            "from decoration_order o " +
            "left join decoration_progress p on p.id = (" +
            "select max(p2.id) from decoration_progress p2 " +
            "where p2.order_id = o.id and p2.is_deleted = 0) " +
            "where o.user_id = #{user_id} and o.is_deleted = 0 " +
            "order by o.id desc")
    List<OrderProgressVO> findOrderProgressByUserId(@Param("user_id") int user_id);
}
