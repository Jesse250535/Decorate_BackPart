package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.DecorationOrder;
import com.example.decoratebackservice.mapper.DecorationOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单业务层
 */
@Service
public class DecorationOrderService {

    @Autowired
    private DecorationOrderMapper decorationOrderMapper;

    /** 订单状态：已支付 */
    public static final int ORDER_STATUS_PAID = 1;
    /** 订单状态：已完成 */
    public static final int ORDER_STATUS_FINISHED = 2;
    /** 订单状态：已退款 */
    public static final int ORDER_STATUS_REFUNDED = 3;

    public int insert(DecorationOrder order) {
        return decorationOrderMapper.insert(order);
    }

    public DecorationOrder findById(int id) {
        return decorationOrderMapper.findById(id);
    }

    public List<DecorationOrder> findAll() {
        return decorationOrderMapper.findAll();
    }

    public int update(DecorationOrder order) {
        return decorationOrderMapper.update(order);
    }

    public int delete(int id) {
        return decorationOrderMapper.delete(id);
    }

    /**
     * 根据订单编号（合同编号）修改合同签订状态
     * 签订时记录签订时间，取消签订时清空签订时间
     *
     * @param order_no 订单编号
     * @param contract_status 合同状态 0未签订1已签订
     * @return 受影响行数
     */
    public int updateContractByOrderNo(String order_no, int contract_status) {
        LocalDateTime contractTime = (contract_status == 1) ? LocalDateTime.now() : null;
        return decorationOrderMapper.updateContractByOrderNo(order_no, contract_status, contractTime);
    }

    /**
     * 根据主键修改订单状态
     *
     * @param id           订单主键
     * @param order_status 目标订单状态 0待支付 1已支付 2已完成 3已退款
     * @return 受影响行数
     */
    public int updateOrderStatus(int id, int order_status) {
        return decorationOrderMapper.updateOrderStatus(id, order_status);
    }

    /**
     * 根据订单编号修改订单状态
     *
     * @param order_no     订单编号
     * @param order_status 目标订单状态 0待支付 1已支付 2已完成 3已退款
     * @return 受影响行数
     */
    public int updateOrderStatusByOrderNo(String order_no, int order_status) {
        return decorationOrderMapper.updateOrderStatusByOrderNo(order_no, order_status);
    }

    /**
     * 用户申请退款：直接把订单状态置为 3（已退款），无需人工审核
     *
     * @param order_no 订单编号
     * @return 受影响行数，0 表示订单不存在
     */
    public int refundByOrderNo(String order_no) {
        return decorationOrderMapper.updateOrderStatusByOrderNo(order_no, ORDER_STATUS_REFUNDED);
    }

    /* ==================== 用户自助下单 / 支付 ==================== */

    /** 订单状态：待支付 */
    public static final int ORDER_STATUS_UNPAID = 0;

    /**
     * 为当前登录用户创建一笔「待支付」订单
     * <p>
     * 订单号、用户ID、各项状态一律由后端决定：下单人只负责说明买什么、花多少钱。
     * 前端传来的 user_id / order_status / pay_time 会被无条件覆盖，
     * 否则用户可以伪造成别人下单，或者直接把自己标成「已支付」。
     *
     * @param userId 当前登录用户ID（来自会话，不是请求体）
     * @param order  前端提交的订单（只用 order_type / target_id / total_amount / remark）
     * @return 落库后的订单，order_no 已由后端生成
     */
    public DecorationOrder createUnpaidOrder(int userId, DecorationOrder order) {
        order.setUser_id(userId);
        order.setOrder_status(ORDER_STATUS_UNPAID);
        order.setContract_status(0);
        order.setRefund_status(0);
        order.setIs_deleted(0);
        order.setPay_time(null);
        order.setContract_time(null);
        order.setFinish_time(null);
        order.setOrder_no(generateOrderNo());
        decorationOrderMapper.insert(order);
        return order;
    }

    /**
     * 支付订单：把自己的待支付订单置为已支付并记录支付时间
     *
     * @param order_no 订单编号
     * @param userId   当前登录用户ID
     * @return 受影响行数，0 表示订单不存在 / 不属于本人 / 已不是待支付状态
     */
    public int payOrder(String order_no, int userId) {
        return decorationOrderMapper.payByOrderNo(order_no, userId, ORDER_STATUS_PAID, LocalDateTime.now());
    }

    /**
     * 生成订单编号：DD + 年月日时分秒 + 4 位随机数
     * <p>
     * order_no 有唯一索引，同一秒内理论上可能撞号，撞了重试几次即可（由调用方捕获异常重试）。
     */
    private String generateOrderNo() {
        LocalDateTime now = LocalDateTime.now();
        String stamp = String.format("%04d%02d%02d%02d%02d%02d",
                now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
                now.getHour(), now.getMinute(), now.getSecond());
        int random = (int) (Math.random() * 9000) + 1000;
        return "DD" + stamp + random;
    }

}
