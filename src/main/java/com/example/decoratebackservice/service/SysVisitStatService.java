package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.SysVisitStat;
import com.example.decoratebackservice.mapper.SysVisitStatMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 系统访问统计业务层
 */
@Service
public class SysVisitStatService {

    @Autowired
    private SysVisitStatMapper sysVisitStatMapper;

    public int insert(SysVisitStat stat) {
        return sysVisitStatMapper.insert(stat);
    }

    public SysVisitStat findById(int id) {
        return sysVisitStatMapper.findById(id);
    }

    public List<SysVisitStat> findAll() {
        return sysVisitStatMapper.findAll();
    }

    public int update(SysVisitStat stat) {
        return sysVisitStatMapper.update(stat);
    }

    public int delete(int id) {
        return sysVisitStatMapper.delete(id);
    }
}
