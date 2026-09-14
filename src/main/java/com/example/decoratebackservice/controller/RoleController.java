package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.Role;
import com.example.decoratebackservice.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色管理接口
 */
@Tag(name = "角色管理接口")
@RestController
@RequestMapping("/role")
@CrossOrigin(origins = "*", maxAge = 3600)
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Operation(summary = "添加角色信息")
    @PostMapping("/insert")
    public Result<Integer> insert(@RequestBody Role role) {
        return Result.success(roleService.insert(role));
    }

    @Operation(summary = "根据ID查询角色信息")
    @GetMapping("/findById/{id}")
    public Result<Role> findById(@PathVariable int id) {
        return Result.success(roleService.findById(id));
    }

    @Operation(summary = "更新角色信息")
    @PutMapping("/update")
    public Result<Integer> update(@RequestBody Role role) {
        return Result.success(roleService.update(role));
    }

    @Operation(summary = "根据ID删除角色")
    @DeleteMapping("/delete/{id}")
    public Result<Integer> delete(@PathVariable int id) {
        return Result.success(roleService.delete(id));
    }

    @Operation(summary = "查询所有角色信息")
    @GetMapping("/findAll")
    public Result<List<Role>> findAll() {
        return Result.success(roleService.findAll());
    }
}
