package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.PaymentRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 支付交易流水 Mapper
 * <p>
 * 所有写操作都带 status 条件，靠数据库保证幂等：
 * 支付宝会重复推送同一笔通知，只有第一次能把 status 从 0 改成 1。
 */
@Mapper
public interface PaymentRecordMapper {

    @Insert("insert into payment_record(out_trade_no, service_id, node_id, user_id, amount, subject, pay_mode, status, create_time) "
            + "values(#{out_trade_no}, #{service_id}, #{node_id}, #{user_id}, #{amount}, #{subject}, #{pay_mode}, #{status}, now())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PaymentRecord record);

    @Results(id = "recordResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "out_trade_no", column = "out_trade_no"),
            @Result(property = "service_id", column = "service_id"),
            @Result(property = "node_id", column = "node_id"),
            @Result(property = "user_id", column = "user_id"),
            @Result(property = "amount", column = "amount"),
            @Result(property = "subject", column = "subject"),
            @Result(property = "trade_no", column = "trade_no"),
            @Result(property = "pay_mode", column = "pay_mode"),
            @Result(property = "status", column = "status"),
            @Result(property = "notify_time", column = "notify_time"),
            @Result(property = "create_time", column = "create_time")
    })
    @Select("select * from payment_record where out_trade_no = #{out_trade_no}")
    PaymentRecord findByOutTradeNo(@Param("out_trade_no") String outTradeNo);

    @ResultMap("recordResult")
    @Select("select * from payment_record where id = #{id}")
    PaymentRecord findById(@Param("id") int id);

    /**
     * 查该付款节点上还没支付的交易：业主重复点「支付」时复用同一笔，避免生成大量脏单
     */
    @ResultMap("recordResult")
    @Select("select * from payment_record where node_id = #{node_id} and status = 0 order by id desc limit 1")
    PaymentRecord findPendingByNode(@Param("node_id") int nodeId);

    @ResultMap("recordResult")
    @Select("select * from payment_record where service_id = #{service_id} order by id desc")
    List<PaymentRecord> findByService(@Param("service_id") int serviceId);

    /**
     * 标记已支付：status = 0 才更新，返回 0 说明已被别的线程/重复回调处理过
     */
    @Update("update payment_record set status = 1, trade_no = #{trade_no}, notify_time = now() "
            + "where id = #{id} and status = 0")
    int markPaid(@Param("id") int id, @Param("trade_no") String tradeNo);

    @Update("update payment_record set status = 2 where id = #{id} and status = 0")
    int close(@Param("id") int id);
}
