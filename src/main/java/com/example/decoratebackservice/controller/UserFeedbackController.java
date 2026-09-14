package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.UserFeedback;
import com.example.decoratebackservice.service.UserFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户意见反馈管理接口
 */
@Tag(name = "用户意见反馈管理接口")
@RestController
@RequestMapping("/feedback")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserFeedbackController {

    @Autowired
    private UserFeedbackService userFeedbackService;

    @Operation(summary = "提交意见反馈")
    @PostMapping("/insert")
    public Result<Integer> insert(@RequestBody UserFeedback feedback) {
        return Result.success(userFeedbackService.insert(feedback));
    }

    @Operation(summary = "根据ID查询反馈")
    @GetMapping("/findById/{id}")
    public Result<UserFeedback> findById(@PathVariable int id) {
        return Result.success(userFeedbackService.findById(id));
    }

    @Operation(summary = "更新反馈")
    @PutMapping("/update")
    public Result<Integer> update(@RequestBody UserFeedback feedback) {
        return Result.success(userFeedbackService.update(feedback));
    }

    @Operation(summary = "根据ID删除反馈")
    @DeleteMapping("/delete/{id}")
    public Result<Integer> delete(@PathVariable int id) {
        return Result.success(userFeedbackService.delete(id));
    }

    @Operation(summary = "查询所有反馈")
    @GetMapping("/findAll")
    public Result<List<UserFeedback>> findAll() {
        return Result.success(userFeedbackService.findAll());
    }
}
