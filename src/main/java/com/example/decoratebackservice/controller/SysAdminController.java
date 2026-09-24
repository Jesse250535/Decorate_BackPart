package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.SysAdmin;
import com.example.decoratebackservice.service.SysAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员账号管理接口
 */
@Tag(name = "管理员账号管理接口")
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SysAdminController {

    @Autowired
    private SysAdminService sysAdminService;

    @Operation(summary = "添加管理员")
    @PostMapping("/insert")
    public Result<Integer> insert(@RequestBody SysAdmin admin) {
        return Result.success(sysAdminService.insert(admin));
    }

    @Operation(summary = "根据ID查询管理员")
    @GetMapping("/findById/{id}")
    public Result<SysAdmin> findById(@PathVariable int id) {
        return Result.success(sysAdminService.findById(id));
    }

    @Operation(summary = "更新管理员")
    @PutMapping("/update")
    public Result<Integer> update(@RequestBody SysAdmin admin) {
        return Result.success(sysAdminService.update(admin));
    }

    @Operation(summary = "根据ID删除管理员")
    @DeleteMapping("/delete/{id}")
    public Result<Integer> delete(@PathVariable int id) {
        return Result.success(sysAdminService.delete(id));
    }

    @Operation(summary = "查询所有管理员")
    @GetMapping("/findAll")
    public Result<List<SysAdmin>> findAll() {
        return Result.success(sysAdminService.findAll());
    }
}
