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

}
