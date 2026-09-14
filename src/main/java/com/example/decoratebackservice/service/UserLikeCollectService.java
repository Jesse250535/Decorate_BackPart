package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.UserLikeCollect;
import com.example.decoratebackservice.mapper.UserLikeCollectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户点赞收藏业务层
 */
@Service
public class UserLikeCollectService {

    @Autowired
    private UserLikeCollectMapper userLikeCollectMapper;

    public int insert(UserLikeCollect likeCollect) {
        return userLikeCollectMapper.insert(likeCollect);
    }

    public UserLikeCollect findById(int id) {
        return userLikeCollectMapper.findById(id);
    }

    public List<UserLikeCollect> findAll() {
        return userLikeCollectMapper.findAll();
    }

    public int update(UserLikeCollect likeCollect) {
        return userLikeCollectMapper.update(likeCollect);
    }

    public int delete(int id) {
        return userLikeCollectMapper.delete(id);
    }
}
