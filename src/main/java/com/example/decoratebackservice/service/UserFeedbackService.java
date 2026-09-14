package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.UserFeedback;
import com.example.decoratebackservice.mapper.UserFeedbackMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户意见反馈业务层
 */
@Service
public class UserFeedbackService {

    @Autowired
    private UserFeedbackMapper userFeedbackMapper;

    public int insert(UserFeedback feedback) {
        return userFeedbackMapper.insert(feedback);
    }

    public UserFeedback findById(int id) {
        return userFeedbackMapper.findById(id);
    }

    public List<UserFeedback> findAll() {
        return userFeedbackMapper.findAll();
    }

    public int update(UserFeedback feedback) {
        return userFeedbackMapper.update(feedback);
    }

    public int delete(int id) {
        return userFeedbackMapper.delete(id);
    }
}
