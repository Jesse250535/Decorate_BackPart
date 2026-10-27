package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.UserLikeCollect;
import com.example.decoratebackservice.service.UserLikeCollectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    /* ==================== 个人中心「我的收藏」 ==================== */

    @Operation(summary = "查询当前登录用户的收藏记录（我的收藏，不含已取消）")
    @GetMapping("/my")
    public Result<List<UserLikeCollect>> myCollections(Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录后再查看收藏记录");
        }
        List<UserLikeCollect> list = userLikeCollectService.findMyCollects(userId);
        if (list.isEmpty()) {
            return Result.success("你还没有收藏任何内容", list);
        }
        return Result.success(list);
    }

    @Operation(summary = "收藏一个方案 / 材料 / 评论")
    @PostMapping("/my")
    public Result<Integer> collect(@RequestBody Map<String, Integer> body, Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录后再收藏");
        }
        Integer targetType = body.get("target_type");
        Integer targetId = body.get("target_id");
        if (targetType == null || targetId == null) {
            return Result.validateFailed("缺少参数：target_type / target_id");
        }
        int rows = userLikeCollectService.collect(userId, targetType, targetId);
        if (rows <= 0) {
            return Result.validateFailed("收藏失败：参数不合法");
        }
        return Result.success("收藏成功", rows);
    }

    @Operation(summary = "取消收藏（逻辑删除）")
    @DeleteMapping("/my")
    public Result<Integer> cancelCollect(@RequestParam("target_type") int targetType,
                                         @RequestParam("target_id") int targetId,
                                         Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录后再取消收藏");
        }
        int rows = userLikeCollectService.cancel(userId, targetType, targetId);
        if (rows <= 0) {
            return Result.error(404, "取消失败：没有找到这条收藏记录");
        }
        return Result.success("已取消收藏", rows);
    }

    @Operation(summary = "查询当前用户是否已收藏某个对象（详情页初始化按钮状态）")
    @GetMapping("/my/check")
    public Result<Boolean> checkCollected(@RequestParam("target_type") int targetType,
                                          @RequestParam("target_id") int targetId,
                                          Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            // 未登录不算错，直接返回未收藏，避免详情页出现无谓的报错
            return Result.success(false);
        }
        return Result.success(userLikeCollectService.isCollected(userId, targetType, targetId));
    }

    /**
     * 从 Security 会话解析当前登录用户ID
     *
     * @param authentication 由 Spring Security 注入
     * @return sys_user.id；未登录或用户不存在时返回 null
     */
    private Integer currentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userLikeCollectService.findUserIdByUsername(authentication.getName());
    }
}
