package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.DecorationProgress;
import com.example.decoratebackservice.service.DecorationProgressService;
import com.example.decoratebackservice.vo.OrderProgressVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 装修进度跟踪管理接口
 */
@Tag(name = "装修进度跟踪管理接口")
@RestController
@RequestMapping("/progress")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DecorationProgressController {

    @Autowired
    private DecorationProgressService decorationProgressService;

    @Operation(summary = "添加上报进度")
    @PostMapping("/insert")
    public Result<Integer> insert(@RequestBody DecorationProgress progress) {
        return Result.success(decorationProgressService.insert(progress));
    }

    @Operation(summary = "根据ID查询进度")
    @GetMapping("/findById/{id}")
    public Result<DecorationProgress> findById(@PathVariable int id) {
        return Result.success(decorationProgressService.findById(id));
    }

    @Operation(summary = "更新进度")
    @PutMapping("/update")
    public Result<Integer> update(@RequestBody DecorationProgress progress) {
        return Result.success(decorationProgressService.update(progress));
    }

    @Operation(summary = "根据ID删除进度")
    @DeleteMapping("/delete/{id}")
    public Result<Integer> delete(@PathVariable int id) {
        return Result.success(decorationProgressService.delete(id));
    }

    @Operation(summary = "查询所有进度")
    @GetMapping("/findAll")
    public Result<List<DecorationProgress>> findAll() {
        return Result.success(decorationProgressService.findAll());
    }

    @Operation(summary = "查询当前登录用户的装修进度（仅订单已支付/已完成可查看）")
    @GetMapping("/my")
    public Result<List<OrderProgressVO>> myProgress(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Result.unauthorized("请先登录后再查看装修进度");
        }
        List<OrderProgressVO> list = decorationProgressService.findMyProgress(authentication.getName());
        if (list.isEmpty()) {
            return Result.success("暂无可查看的装修进度", list);
        }
        return Result.success(list);
    }
}
