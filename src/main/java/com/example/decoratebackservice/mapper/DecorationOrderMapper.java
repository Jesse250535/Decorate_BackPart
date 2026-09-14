package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.DecorationOrder;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 订单 Mapper
 */
@Mapper
public interface DecorationOrderMapper {

    @Insert("insert into decoration_order (order_no,user_id,order_type,target_id,total_amount,order_status," +
            "contract_status,refund_status,remark,pay_time,contract_time,finish_time,is_deleted) " +
            "values(#{order_no},#{user_id},#{order_type},#{target_id},#{total_amount},#{order_status}," +
            "#{contract_status},#{refund_status},#{remark},#{pay_time},#{contract_time},#{finish_time},#{is_deleted})")
    int insert(DecorationOrder order);

    @Results(id = "orderResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "order_no", column = "order_no"),
            @Result(property = "user_id", column = "user_id"),
            @Result(property = "order_type", column = "order_type"),
            @Result(property = "target_id", column = "target_id"),
            @Result(property = "total_amount", column = "total_amount"),
            @Result(property = "order_status", column = "order_status"),
            @Result(property = "contract_status", column = "contract_status"),
            @Result(property = "refund_status", column = "refund_status"),
            @Result(property = "remark", column = "remark"),
            @Result(property = "pay_time", column = "pay_time"),
            @Result(property = "contract_time", column = "contract_time"),
            @Result(property = "finish_time", column = "finish_time"),
            @Result(property = "is_deleted", column = "is_deleted")
    })
    @Select("select * from decoration_order where id = #{id}")
    DecorationOrder findById(@Param("id") int id);

    @ResultMap("orderResult")
    @Select("select * from decoration_order")
    List<DecorationOrder> findAll();

    @Update("update decoration_order set order_no=#{order_no},user_id=#{user_id},order_type=#{order_type}," +
            "target_id=#{target_id},total_amount=#{total_amount},order_status=#{order_status}," +
            "contract_status=#{contract_status},refund_status=#{refund_status},remark=#{remark}," +
            "pay_time=#{pay_time},contract_time=#{contract_time},finish_time=#{finish_time}," +
            "is_deleted=#{is_deleted} where id=#{id}")
    int update(DecorationOrder order);

    @Delete("delete from decoration_order where id = #{id}")
    int delete(@Param("id") int id);
}
