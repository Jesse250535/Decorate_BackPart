package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.Supplier;
import com.example.decoratebackservice.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 材料供应商管理接口
 */
@Tag(name = "材料供应商管理接口")
@RestController
@RequestMapping("/supplier")
@CrossOrigin(origins = "*", maxAge = 3600)
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    @Operation(summary = "添加供应商信息")
    @PostMapping("/insert")
    public Result<Integer> insert(@RequestBody Supplier supplier) {
        return Result.success(supplierService.insert(supplier));
    }

    @Operation(summary = "根据ID查询供应商信息")
    @GetMapping("/findById/{id}")
    public Result<Supplier> findById(@PathVariable int id) {
        return Result.success(supplierService.findById(id));
    }

    @Operation(summary = "更新供应商信息")
    @PutMapping("/update")
    public Result<Integer> update(@RequestBody Supplier supplier) {
        return Result.success(supplierService.update(supplier));
    }

    @Operation(summary = "根据ID删除供应商")
    @DeleteMapping("/delete/{id}")
    public Result<Integer> delete(@PathVariable int id) {
        return Result.success(supplierService.delete(id));
    }

    @Operation(summary = "查询所有供应商信息")
    @GetMapping("/findAll")
    public Result<List<Supplier>> findAll() {
        return Result.success(supplierService.findAll());
    }
}
