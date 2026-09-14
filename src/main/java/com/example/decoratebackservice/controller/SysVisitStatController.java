package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.SysVisitStat;
import com.example.decoratebackservice.service.SysVisitStatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统访问统计管理接口
 */
@Tag(name = "系统访问统计管理接口")
@RestController
@RequestMapping("/visitStat")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SysVisitStatController {

    @Autowired
    private SysVisitStatService sysVisitStatService;

    @Operation(summary = "添加访问统计")
    @PostMapping("/insert")
    public Result<Integer> insert(@RequestBody SysVisitStat stat) {
        return Result.success(sysVisitStatService.insert(stat));
    }

    @Operation(summary = "根据ID查询访问统计")
    @GetMapping("/findById/{id}")
    public Result<SysVisitStat> findById(@PathVariable int id) {
        return Result.success(sysVisitStatService.findById(id));
    }

    @Operation(summary = "更新访问统计")
    @PutMapping("/update")
    public Result<Integer> update(@RequestBody SysVisitStat stat) {
        return Result.success(sysVisitStatService.update(stat));
    }

    @Operation(summary = "根据ID删除访问统计")
    @DeleteMapping("/delete/{id}")
    public Result<Integer> delete(@PathVariable int id) {
        return Result.success(sysVisitStatService.delete(id));
    }

    @Operation(summary = "查询所有访问统计")
    @GetMapping("/findAll")
    public Result<List<SysVisitStat>> findAll() {
        return Result.success(sysVisitStatService.findAll());
    }
}
