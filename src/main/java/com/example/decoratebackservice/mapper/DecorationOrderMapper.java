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
    int updateContractStatus(@Param("id") int id, @Param("contract_status") int contract_status);

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

    /**
     * 根据主键修改订单状态（退款时传 3）
     * 订单状态：0待支付 1已支付 2已完成 3已退款
     *
     * @param id           订单主键
     * @param order_status 目标订单状态
     * @return 受影响行数
     */
    @Update("update decoration_order set order_status=#{order_status} where id=#{id}")
    int updateOrderStatus(@Param("id") int id, @Param("order_status") int order_status);

    /**
     * 根据订单编号修改订单状态（退款时传 3）
     * 订单状态：0待支付 1已支付 2已完成 3已退款
     *
     * @param order_no     订单编号
     * @param order_status 目标订单状态
     * @return 受影响行数
     */
    @Update("update decoration_order set order_status=#{order_status} where order_no=#{order_no}")
    int updateOrderStatusByOrderNo(@Param("order_no") String order_no,
                                   @Param("order_status") int order_status);

    /**
     * 支付：只允许把「自己的、待支付」订单置为已支付，并写入支付时间。
     * <p>
     * 条件里带上 user_id 与 order_status=0，避免用别人的订单号支付或者重复支付；
     * 受影响行数为 0 即代表订单不存在 / 不是本人 / 已不是待支付状态。
     *
     * @param order_no     订单编号
     * @param user_id      下单用户ID
     * @param order_status 目标订单状态（传 1 已支付）
     * @param pay_time     支付时间
     * @return 受影响行数
     */
    @Update("update decoration_order set order_status=#{order_status},pay_time=#{pay_time} " +
            "where order_no=#{order_no} and user_id=#{user_id} and order_status=0 and is_deleted=0")
    int payByOrderNo(@Param("order_no") String order_no,
                     @Param("user_id") int user_id,
                     @Param("order_status") int order_status,
                     @Param("pay_time") LocalDateTime pay_time);

    /**
     * 按订单编号查询（financial 核对金额前先确认合同存在）
     *
     * @param order_no 订单编号
     * @return 订单
     */
    @ResultMap("orderResult")
    @Select("select * from decoration_order where order_no = #{order_no} and is_deleted = 0")
    DecorationOrder findByOrderNo(@Param("order_no") String order_no);

    /**
     * 财务核对合同金额：只改 total_amount，不动订单状态与其它字段
     * <p>
     * 复用 update 会受 {@code <if test='order_status != 0'>} 之类条件影响，
     * 这里用专用语句，保证「只改金额」这个动作不会误伤订单状态。
     *
     * @param order_no     订单（合同）编号
     * @param total_amount 核对后的合同金额
     * @return 受影响行数
     */
    @Update("update decoration_order set total_amount=#{total_amount} where order_no=#{order_no} and is_deleted=0")
    int updateAmountByOrderNo(@Param("order_no") String order_no,
                              @Param("total_amount") java.math.BigDecimal total_amount);

    /**
     * 查询指定合同状态的订单（项目经理看已签订项目、财务看全部合同）
     *
     * @param contract_status 合同状态 0未签订 1已签订
     * @return 订单列表
     */
    @ResultMap("orderResult")
    @Select("select * from decoration_order where contract_status=#{contract_status} and is_deleted=0 " +
            "order by id desc")
    List<DecorationOrder> findByContractStatus(@Param("contract_status") int contract_status);

}
