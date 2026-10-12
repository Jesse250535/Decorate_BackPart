package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.DecorationOrder;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
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

    @Update("<script>" +
            "update decoration_order " +
            "<set>" +
            "<if test='order_no != null'>order_no=#{order_no},</if>" +
            "<if test='user_id != 0'>user_id=#{user_id},</if>" +
            "<if test='order_type != 0'>order_type=#{order_type},</if>" +
            "<if test='target_id != 0'>target_id=#{target_id},</if>" +
            "<if test='total_amount != null'>total_amount=#{total_amount},</if>" +
            "<if test='order_status != 0'>order_status=#{order_status},</if>" +
            "<if test='contract_status != 0'>contract_status=#{contract_status},</if>" +
            "<if test='refund_status != 0'>refund_status=#{refund_status},</if>" +
            "<if test='remark != null'>remark=#{remark},</if>" +
            "<if test='pay_time != null'>pay_time=#{pay_time},</if>" +
            "<if test='contract_time != null'>contract_time=#{contract_time},</if>" +
            "<if test='finish_time != null'>finish_time=#{finish_time},</if>" +
            "<if test='is_deleted != 0'>is_deleted=#{is_deleted},</if>" +
            "</set>" +
            "where id=#{id}" +
            "</script>")
    int update(DecorationOrder order);

    @Delete("delete from decoration_order where id = #{id}")
    int delete(@Param("id") int id);

    @Update("update decoration_order set contract_status=#{contract_status} where id=#{id}")
    int updateContractStatus(@Param("id") int id, @Param("contract_status") String contract_status);

    /**
     * 根据订单编号（合同编号）修改合同签订状态
     * @param order_no 订单编号
     * @param contract_status 合同状态 0未签订1已签订
     * @param contract_time 合同签订时间（未签订时为 null）
     */
    @Update("update decoration_order set contract_status=#{contract_status},contract_time=#{contract_time} " +
            "where order_no=#{order_no}")
    int updateContractByOrderNo(@Param("order_no") String order_no,
                                @Param("contract_status") int contract_status,
                                @Param("contract_time") LocalDateTime contract_time);
}
