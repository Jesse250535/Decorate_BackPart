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
