package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.DecorationComment;
import com.example.decoratebackservice.mapper.DecorationCommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 评论业务层
 */
@Service
public class DecorationCommentService {

    @Autowired
    private DecorationCommentMapper decorationCommentMapper;

    public int insert(DecorationComment comment) {
        return decorationCommentMapper.insert(comment);
    }

    public DecorationComment findById(int id) {
        return decorationCommentMapper.findById(id);
    }

    public List<DecorationComment> findAll() {
        return decorationCommentMapper.findAll();
    }

    public int update(DecorationComment comment) {
        return decorationCommentMapper.update(comment);
    }

    public int delete(int id) {
        return decorationCommentMapper.delete(id);
    }
}
