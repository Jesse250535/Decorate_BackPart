package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.SysNotice;
import com.example.decoratebackservice.service.SysNoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统公告管理接口
 */
@Tag(name = "系统公告管理接口")
@RestController
@RequestMapping("/notice")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SysNoticeController {

    @Autowired
    private SysNoticeService sysNoticeService;

    @Operation(summary = "新增公告")
    @PostMapping("/insert")
    public Result<Integer> insert(@RequestBody SysNotice notice) {
        return Result.success(sysNoticeService.insert(notice));
    }

    @Operation(summary = "根据ID查询公告")
    @GetMapping("/findById/{id}")
    public Result<SysNotice> findById(@PathVariable Long id) {
        return Result.success(sysNoticeService.findById(id));
    }

    @Operation(summary = "查询所有公告")
    @GetMapping("/findAll")
    public Result<List<SysNotice>> findAll() {
        return Result.success(sysNoticeService.findAll());
    }

    /**
     * 前台公开公告列表：公司介绍页「公司公告」与公告中心共用。
     * <p>
     * 只返回已发布（status=1）且未删除的公告，游客未登录也能访问（SecurityConfig 已放行）。
     *
     * @param show_position 展示位置 1首页顶部 2用户中心 3后台首页，为空则不限
     * @param active_only   是否只取未过期公告，默认 false
     * @param limit         最多返回条数，为空则不限
     */
    @Operation(summary = "前台公开公告列表（仅已发布，无需登录）")
    @GetMapping("/list")
    public Result<List<SysNotice>> listPublished(
            @RequestParam(required = false) Integer show_position,
            @RequestParam(required = false, defaultValue = "false") Boolean active_only,
            @RequestParam(required = false) Integer limit) {
        return Result.success(sysNoticeService.listPublished(show_position, active_only, limit));
    }

    @Operation(summary = "按发布状态/展示位置查询公告")
    @GetMapping("/findByCondition")
    public Result<List<SysNotice>> findByCondition(@RequestParam(required = false) Integer status,
                                                   @RequestParam(required = false) Integer show_position) {
        return Result.success(sysNoticeService.findByCondition(status, show_position));
    }

    @Operation(summary = "更新公告")
    @PutMapping("/update")
    public Result<Integer> update(@RequestBody SysNotice notice) {
        return Result.success(sysNoticeService.update(notice));
    }

    @Operation(summary = "根据ID删除公告")
    @DeleteMapping("/delete/{id}")
    public Result<Integer> delete(@PathVariable Long id) {
        return Result.success(sysNoticeService.deleteById(id));
    }
}
