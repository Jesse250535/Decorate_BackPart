package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.DecorationComment;
import com.example.decoratebackservice.service.DecorationCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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

    /* ==================== 个人中心「我的评价」 ==================== */

    @Operation(summary = "查询当前登录用户的评论记录（我的评价，不含已删除）")
    @GetMapping("/my")
    public Result<List<DecorationComment>> myComments(Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录后再查看评论记录");
        }
        List<DecorationComment> list = decorationCommentService.findMyComments(userId);
        if (list.isEmpty()) {
            return Result.success("你还没有发布过评论", list);
        }
        return Result.success(list);
    }

    @Operation(summary = "修改自己的评论（仅内容与图片）")
    @PutMapping("/my/update/{id}")
    public Result<Integer> updateMyComment(@PathVariable int id,
                                           @RequestBody DecorationComment comment,
                                           Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录后再修改评论");
        }
        if (comment.getContent() == null || comment.getContent().isBlank()) {
            return Result.validateFailed("评论内容不能为空");
        }
        int rows = decorationCommentService.updateMyComment(userId, id,
                comment.getContent(), comment.getImages());
        if (rows <= 0) {
            return Result.error(403, "修改失败：评论不存在或不属于你");
        }
        return Result.success("修改成功", rows);
    }

    @Operation(summary = "删除自己的评论（逻辑删除）")
    @DeleteMapping("/my/delete/{id}")
    public Result<Integer> deleteMyComment(@PathVariable int id, Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录后再删除评论");
        }
        int rows = decorationCommentService.deleteMyComment(userId, id);
        if (rows <= 0) {
            return Result.error(403, "删除失败：评论不存在或不属于你");
        }
        return Result.success("删除成功", rows);
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
        return decorationCommentService.findUserIdByUsername(authentication.getName());
    }
}
