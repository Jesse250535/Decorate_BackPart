package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.SysAdmin;
import com.example.decoratebackservice.mapper.SysAdminMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 管理员账号业务层
 */
@Service
public class SysAdminService {

    @Autowired
    private SysAdminMapper sysAdminMapper;

    public int insert(SysAdmin admin) {
        return sysAdminMapper.insert(admin);
    }

    public SysAdmin findById(int id) {
        return sysAdminMapper.findById(id);
    }

    public SysAdmin findByUsername(String username) {
        return sysAdminMapper.findByUsername(username);
    }

    public List<SysAdmin> findAll() {
        return sysAdminMapper.findAll();
    }

    public int update(SysAdmin admin) {
        return sysAdminMapper.update(admin);
    }

    public int delete(int id) {
        return sysAdminMapper.delete(id);
    }
}
