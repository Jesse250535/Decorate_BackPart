package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.DecorationProgress;
import com.example.decoratebackservice.mapper.DecorationProgressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 装修进度跟踪业务层
 */
@Service
public class DecorationProgressService {

    @Autowired
    private DecorationProgressMapper decorationProgressMapper;

    public int insert(DecorationProgress progress) {
        return decorationProgressMapper.insert(progress);
    }

    public DecorationProgress findById(int id) {
        return decorationProgressMapper.findById(id);
    }

    public List<DecorationProgress> findAll() {
        return decorationProgressMapper.findAll();
    }

    public int update(DecorationProgress progress) {
        return decorationProgressMapper.update(progress);
    }

    public int delete(int id) {
        return decorationProgressMapper.delete(id);
    }
}
