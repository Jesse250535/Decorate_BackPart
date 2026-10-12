package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.SysNotice;
import com.example.decoratebackservice.mapper.SysNoticeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 系统公告业务层
 */
@Service
public class SysNoticeService {

    @Autowired
    private SysNoticeMapper sysNoticeMapper;

    /**
     * 新增公告
     * 发布时间 / 发布状态 / 逻辑删除标记未传时补默认值，保证非空约束
     */
    public int insert(SysNotice notice) {
        if (notice.getPublish_time() == null) {
            notice.setPublish_time(LocalDateTime.now());
        }
        if (notice.getStatus() == null) {
            notice.setStatus(0);
        }
        if (notice.getIs_deleted() == null) {
            notice.setIs_deleted(0);
        }
        return sysNoticeMapper.insert(notice);
    }

    public SysNotice findById(Long id) {
        return sysNoticeMapper.findById(id);
    }

    public List<SysNotice> findAll() {
        return sysNoticeMapper.findAll();
    }

    public List<SysNotice> findByCondition(Integer status, Integer show_position) {
        return sysNoticeMapper.findByCondition(status, show_position);
    }

    public int update(SysNotice notice) {
        return sysNoticeMapper.update(notice);
    }

    public int deleteById(Long id) {
        return sysNoticeMapper.deleteById(id);
    }
}
