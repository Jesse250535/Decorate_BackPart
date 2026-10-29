package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.ConstructionPaymentNode;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 分期付款节点 Mapper
 */
@Mapper
public interface ConstructionPaymentNodeMapper {

    @Insert("insert into construction_payment_node (service_id,node_code,node_name,ratio,amount,trigger_stage_code," +
            "pay_status,remark) values(#{service_id},#{node_code},#{node_name},#{ratio},#{amount}," +
            "#{trigger_stage_code},#{pay_status},#{remark})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ConstructionPaymentNode node);

    @Results(id = "pnodeResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "service_id", column = "service_id"),
            @Result(property = "node_code", column = "node_code"),
            @Result(property = "node_name", column = "node_name"),
            @Result(property = "ratio", column = "ratio"),
            @Result(property = "amount", column = "amount"),
            @Result(property = "trigger_stage_code", column = "trigger_stage_code"),
            @Result(property = "pay_status", column = "pay_status"),
            @Result(property = "pay_time", column = "pay_time"),
            @Result(property = "trade_no", column = "trade_no"),
            @Result(property = "remark", column = "remark"),
            @Result(property = "create_time", column = "create_time"),
            @Result(property = "update_time", column = "update_time")
    })
    @Select("select * from construction_payment_node where id = #{id}")
    ConstructionPaymentNode findById(@Param("id") int id);

    @ResultMap("pnodeResult")
    @Select("select * from construction_payment_node where service_id = #{service_id} " +
            "order by field(node_code,'DEPOSIT','TILING','CUSTOM','FINAL')")
    List<ConstructionPaymentNode> findByService(@Param("service_id") int serviceId);

    @ResultMap("pnodeResult")
    @Select("select * from construction_payment_node where service_id = #{service_id} and node_code = #{node_code}")
    ConstructionPaymentNode findByServiceAndCode(@Param("service_id") int serviceId,
                                                 @Param("node_code") String nodeCode);

    @Select("select count(*) from construction_payment_node where service_id = #{service_id}")
    int countByService(@Param("service_id") int serviceId);

    @Select("select coalesce(sum(amount),0) from construction_payment_node where service_id = #{service_id} " +
            "and pay_status = 1")
    BigDecimal sumPaid(@Param("service_id") int serviceId);

    /**
     * 支付节点：只有「待支付」才生效，重复支付返回 0 行
     */
    @Update("update construction_payment_node set pay_status = 1, pay_time = #{time}, trade_no = #{trade_no} " +
            "where id = #{id} and pay_status = 0")
    int pay(@Param("id") int id, @Param("trade_no") String tradeNo, @Param("time") LocalDateTime time);

    /**
     * 按总额比例回填金额（报价确认或合同签订时重算）
     */
    @Update("update construction_payment_node set ratio = #{ratio}, amount = #{amount} " +
            "where id = #{id} and pay_status = 0")
    int updateAmount(@Param("id") int id, @Param("ratio") BigDecimal ratio, @Param("amount") BigDecimal amount);

    @Update("update construction_payment_node set remark = #{remark} where id = #{id}")
    int updateRemark(@Param("id") int id, @Param("remark") String remark);
}
