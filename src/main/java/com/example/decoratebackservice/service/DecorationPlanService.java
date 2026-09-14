package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.DecorationPlan;
import com.example.decoratebackservice.mapper.DecorationPlanMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 装修方案业务层
 */
@Service
public class DecorationPlanService {

    @Autowired
    private DecorationPlanMapper decorationPlanMapper;

    public int insert(DecorationPlan plan) {
        return decorationPlanMapper.insert(plan);
    }

    public DecorationPlan findById(int id) {
        return decorationPlanMapper.findById(id);
    }

    public List<DecorationPlan> findAll() {
        return decorationPlanMapper.findAll();
    }

    public int update(DecorationPlan plan) {
        return decorationPlanMapper.update(plan);
    }

    public int delete(int id) {
        return decorationPlanMapper.delete(id);
    }
}
