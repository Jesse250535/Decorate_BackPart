package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.UserLikeCollect;
import com.example.decoratebackservice.service.UserLikeCollectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户点赞收藏管理接口
 */
@Tag(name = "用户点赞收藏管理接口")
@RestController
@RequestMapping("/likeCollect")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UserLikeCollectController {

    @Autowired
    private UserLikeCollectService userLikeCollectService;

    @Operation(summary = "添加点赞收藏")
    @PostMapping("/insert")
    public Result<Integer> insert(@RequestBody UserLikeCollect likeCollect) {
        return Result.success(userLikeCollectService.insert(likeCollect));
    }

    @Operation(summary = "根据ID查询点赞收藏")
    @GetMapping("/findById/{id}")
    public Result<UserLikeCollect> findById(@PathVariable int id) {
        return Result.success(userLikeCollectService.findById(id));
    }

    @Operation(summary = "更新点赞收藏")
    @PutMapping("/update")
    public Result<Integer> update(@RequestBody UserLikeCollect likeCollect) {
        return Result.success(userLikeCollectService.update(likeCollect));
    }

    @Operation(summary = "根据ID删除点赞收藏")
    @DeleteMapping("/delete/{id}")
    public Result<Integer> delete(@PathVariable int id) {
        return Result.success(userLikeCollectService.delete(id));
    }

    @Operation(summary = "查询所有点赞收藏")
    @GetMapping("/findAll")
    public Result<List<UserLikeCollect>> findAll() {
        return Result.success(userLikeCollectService.findAll());
    }
}
