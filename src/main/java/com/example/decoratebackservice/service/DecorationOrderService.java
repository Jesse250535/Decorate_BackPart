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

}
