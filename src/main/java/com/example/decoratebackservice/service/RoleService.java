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

    /**
     * 按角色标识查询（如 ROLE_SUPPLIER）
     * <p>
     * 注册页传的是 role_code，而 sys_user 存的 role_id，需要先换算。
     * 角色表数据量极小（8 条），直接内存过滤即可，不必再加一条 SQL。
     *
     * @param roleCode 角色标识
     * @return 角色；不存在返回 null
     */
    public Role findByCode(String roleCode) {
        if (roleCode == null || roleCode.isBlank()) {
            return null;
        }
        for (Role role : roleMapper.findAll()) {
            if (roleCode.equals(role.getRole_code())) {
                return role;
            }
        }
        return null;
    }

    public int update(Role role) {
        return roleMapper.update(role);
    }

    public int delete(int id) {
        return roleMapper.delete(id);
    }
}
