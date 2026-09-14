package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.DecorationOrder;
import com.example.decoratebackservice.mapper.DecorationOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
