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

    /**
     * 前台公开公告列表（公司公告 / 公告中心）
     * <p>
     * 只返回已发布（status=1）且未逻辑删除的公告；
     * activeOnly 为 true 时额外过滤掉已过 expire_time 的公告；
     * limit 为空表示不限制条数，非法值（<=0）按 1 条处理，超过 100 按 100 截断。
     *
     * @param show_position 展示位置，可为空
     * @param activeOnly    是否只取未过期公告
     * @param limit         最多返回条数，可为空
     * @return 公告列表，按发布时间倒序
     */
    public List<SysNotice> listPublished(Integer show_position, Boolean activeOnly, Integer limit) {
        Integer safeLimit = null;
        if (limit != null) {
            safeLimit = Math.max(1, Math.min(limit, 100));
        }
        return sysNoticeMapper.findPublished(show_position, Boolean.TRUE.equals(activeOnly), safeLimit);
    }

    public int update(SysNotice notice) {
        return sysNoticeMapper.update(notice);
    }

    public int deleteById(Long id) {
        return sysNoticeMapper.deleteById(id);
    }
}
