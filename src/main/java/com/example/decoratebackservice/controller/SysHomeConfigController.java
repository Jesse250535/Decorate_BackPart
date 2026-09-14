package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.SysHomeConfig;
import com.example.decoratebackservice.service.SysHomeConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 首页配置管理接口
 */
@Tag(name = "首页配置管理接口")
@RestController
@RequestMapping("/homeConfig")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SysHomeConfigController {

    @Autowired
    private SysHomeConfigService sysHomeConfigService;

    @Operation(summary = "添加首页配置")
    @PostMapping("/insert")
    public Result<Integer> insert(@RequestBody SysHomeConfig config) {
        return Result.success(sysHomeConfigService.insert(config));
    }

    @Operation(summary = "根据ID查询首页配置")
    @GetMapping("/findById/{id}")
    public Result<SysHomeConfig> findById(@PathVariable int id) {
        return Result.success(sysHomeConfigService.findById(id));
    }

    @Operation(summary = "更新首页配置")
    @PutMapping("/update")
    public Result<Integer> update(@RequestBody SysHomeConfig config) {
        return Result.success(sysHomeConfigService.update(config));
    }

    @Operation(summary = "根据ID删除首页配置")
    @DeleteMapping("/delete/{id}")
    public Result<Integer> delete(@PathVariable int id) {
        return Result.success(sysHomeConfigService.delete(id));
    }

    @Operation(summary = "查询所有首页配置")
    @GetMapping("/findAll")
    public Result<List<SysHomeConfig>> findAll() {
        return Result.success(sysHomeConfigService.findAll());
    }
}
