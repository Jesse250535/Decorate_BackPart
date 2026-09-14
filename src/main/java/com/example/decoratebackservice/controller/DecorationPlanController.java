package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.DecorationPlan;
import com.example.decoratebackservice.service.DecorationPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 装修方案管理接口
 */
@Tag(name = "装修方案管理接口")
@RestController
@RequestMapping("/plan")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DecorationPlanController {

    @Autowired
    private DecorationPlanService decorationPlanService;

    @Operation(summary = "添加装修方案")
    @PostMapping("/insert")
    public Result<Integer> insert(@RequestBody DecorationPlan plan) {
        return Result.success(decorationPlanService.insert(plan));
    }

    @Operation(summary = "根据ID查询装修方案")
    @GetMapping("/findById/{id}")
    public Result<DecorationPlan> findById(@PathVariable int id) {
        return Result.success(decorationPlanService.findById(id));
    }

    @Operation(summary = "更新装修方案")
    @PutMapping("/update")
    public Result<Integer> update(@RequestBody DecorationPlan plan) {
        return Result.success(decorationPlanService.update(plan));
    }

    @Operation(summary = "根据ID删除装修方案")
    @DeleteMapping("/delete/{id}")
    public Result<Integer> delete(@PathVariable int id) {
        return Result.success(decorationPlanService.delete(id));
    }

    @Operation(summary = "查询所有装修方案")
    @GetMapping("/findAll")
    public Result<List<DecorationPlan>> findAll() {
        return Result.success(decorationPlanService.findAll());
    }
}
