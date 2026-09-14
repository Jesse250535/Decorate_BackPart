package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.SysHomeConfig;
import com.example.decoratebackservice.mapper.SysHomeConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 首页配置业务层
 */
@Service
public class SysHomeConfigService {

    @Autowired
    private SysHomeConfigMapper sysHomeConfigMapper;

    public int insert(SysHomeConfig config) {
        return sysHomeConfigMapper.insert(config);
    }

    public SysHomeConfig findById(int id) {
        return sysHomeConfigMapper.findById(id);
    }

    public List<SysHomeConfig> findAll() {
        return sysHomeConfigMapper.findAll();
    }

    public int update(SysHomeConfig config) {
        return sysHomeConfigMapper.update(config);
    }

    public int delete(int id) {
        return sysHomeConfigMapper.delete(id);
    }
}
