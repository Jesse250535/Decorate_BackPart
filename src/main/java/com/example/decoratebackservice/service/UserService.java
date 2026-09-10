package com.example.decoratebackservice.service;

import com.example.decoratebackservice.mapper.UserMapper;
import com.example.decoratebackservice.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @BelongsProject:dream_house
 * @BelongsPackage:com.example.dream_house.service
 * @Author:Uestc_Xiye
 * @CreateTime:2023-12-17 17:04:45
 */

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;


    /*
     * 新增信息
     *
     * @param id
     *@param username
     * @param phone
     * @param password
     * @param avatar
     * @param nickname
     * @param gender
     * @param user_type
     * @param role_id
     * @param profile
     * @param violation_count
     * @param status
     * @param is_deleted
     */
    public String insert(String username, String phone, String password, String avatar, String nickname, String gender, String user_type, int role_id, String profile, int violation_count, String status, int is_deleted) {
        userMapper.insert(username, phone, password, avatar, nickname, gender, user_type, role_id, profile, violation_count, status, is_deleted);
        return "succeed";
    }

    /**
     * 查询id对应的信息
     *
     * @param id
     * @return
     */
    public User findById(int id) {
        return userMapper.findById(id);
    }

    public List<User> findAll() {
        return userMapper.findAll();
    }

    /**
     * 更新信息
     *
     * @param data
     */
    public void update(User data) {
        userMapper.update(data);
    }

    /**
     * 删除id对应的信息
     *
     * @param id
     */
    public void delete(int id) {
        userMapper.delete(id);
    }
}
