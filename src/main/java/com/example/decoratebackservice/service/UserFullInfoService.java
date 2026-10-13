package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.UserFullInfo;
import com.example.decoratebackservice.mapper.UserFullInfoMapper;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 用户完整信息业务层：查询 sys_user 的全部字段
 */
@Service
public class UserFullInfoService {

    private final UserFullInfoMapper userFullInfoMapper;

    public UserFullInfoService(UserFullInfoMapper userFullInfoMapper) {
        this.userFullInfoMapper = userFullInfoMapper;
    }

    /**
     * 按 id 查询单个用户完整信息（password 置空，避免泄露）
     */
    public UserFullInfo findById(int id) {
        UserFullInfo user = userFullInfoMapper.findById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }

    /**
     * 查询所有用户完整信息（password 置空，避免泄露）
     */
    public List<UserFullInfo> findAll() {
        List<UserFullInfo> users = userFullInfoMapper.findAll();
        for (UserFullInfo user : users) {
            user.setPassword(null);
        }
        return users;
    }

    /**
     * 按用户名查询单个用户完整信息（password 置空，避免泄露）
     *
     * @param username 用户名
     * @return 用户完整信息，用户名不存在时返回 null
     */
    public UserFullInfo findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        UserFullInfo user = userFullInfoMapper.findByUsername(username.trim());
        if (user != null) {
            user.setPassword(null);
        }
        return user;
    }
}
