package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.DecorationComment;
import com.example.decoratebackservice.service.DecorationCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 评论管理接口
 */
@Tag(name = "评论管理接口")
@RestController
@RequestMapping("/comment")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DecorationCommentController {

    @Autowired
    private DecorationCommentService decorationCommentService;

    @Operation(summary = "添加评论")
    @PostMapping("/insert")
    public Result<Integer> insert(@RequestBody DecorationComment comment) {
        return Result.success(decorationCommentService.insert(comment));
    }

    @Operation(summary = "根据ID查询评论")
    @GetMapping("/findById/{id}")
    public Result<DecorationComment> findById(@PathVariable int id) {
        return Result.success(decorationCommentService.findById(id));
    }

    @Operation(summary = "更新评论")
    @PutMapping("/update")
    public Result<Integer> update(@RequestBody DecorationComment comment) {
        return Result.success(decorationCommentService.update(comment));
    }

    @Operation(summary = "根据ID删除评论")
    @DeleteMapping("/delete/{id}")
    public Result<Integer> delete(@PathVariable int id) {
        return Result.success(decorationCommentService.delete(id));
    }

    @Operation(summary = "查询所有评论")
    @GetMapping("/findAll")
    public Result<List<DecorationComment>> findAll() {
        return Result.success(decorationCommentService.findAll());
    }
}
