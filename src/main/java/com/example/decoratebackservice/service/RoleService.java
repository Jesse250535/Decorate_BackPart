package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.Role;
import com.example.decoratebackservice.mapper.RoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 角色业务层
 */
@Service
public class RoleService {

    @Autowired
    private RoleMapper roleMapper;

    public int insert(Role role) {
        return roleMapper.insert(role);
    }

    public Role findById(int id) {
        return roleMapper.findById(id);
    }

    public List<Role> findAll() {
        return roleMapper.findAll();
    }

    public int update(Role role) {
        return roleMapper.update(role);
    }

    public int delete(int id) {
        return roleMapper.delete(id);
    }
}
