package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.config.CommentRequest;
import com.example.decoratebackservice.entity.DecorationComment;
import com.example.decoratebackservice.service.DecorationCommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    /* ==================== 详情页评论区（方案 / 材料） ==================== */

    /**
     * 评论列表：主评论 + 回复，并标记当前登录用户是否点过赞。
     * <p>
     * 该接口在 SecurityConfig 中放行，未登录也能浏览评论，
     * 此时 liked 恒为 false。
     */
    @Operation(summary = "查询某个方案/材料下的评论与回复")
    @GetMapping("/list")
    public Result<List<DecorationComment>> list(@RequestParam("target_type") int targetType,
                                                @RequestParam("target_id") int targetId,
                                                Authentication authentication) {
        Integer userId = currentUserId(authentication);
        return Result.success(decorationCommentService.listByTarget(targetType, targetId, userId));
    }

    @Operation(summary = "发表评论 / 回复（登录后）")
    @PostMapping("/publish")
    public Result<DecorationComment> publish(@RequestBody CommentRequest body,
                                             Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录后再发表评价");
        }
        if (body == null || body.getTarget_type() == null || body.getTarget_id() == null) {
            return Result.validateFailed("缺少评论对象");
        }
        String content = body.getContent();
        if (content == null || content.isBlank()) {
            return Result.validateFailed("评论内容不能为空");
        }
        if (content.trim().length() > DecorationCommentService.MAX_CONTENT_LENGTH) {
            return Result.validateFailed("评论内容不能超过 " + DecorationCommentService.MAX_CONTENT_LENGTH + " 字");
        }
        int parentId = body.getParent_id() == null ? 0 : body.getParent_id();
        DecorationComment saved = decorationCommentService.publish(
                userId, body.getTarget_type(), body.getTarget_id(), parentId, content);
        if (saved == null) {
            return Result.error(400, "发表失败：评论对象不存在，或要回复的评论已被删除");
        }
        return Result.success(parentId > 0 ? "回复成功" : "评价发表成功", saved);
    }

    /**
     * 点赞 / 取消点赞（同一个接口按当前状态切换）
     */
    @Operation(summary = "点赞或取消点赞某条评论")
    @PostMapping("/{id}/like")
    public Result<Map<String, Object>> toggleLike(@PathVariable int id, Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录后再点赞");
        }
        Map<String, Object> data = decorationCommentService.toggleCommentLike(userId, id);
        if (data == null) {
            return Result.error(404, "评论不存在或已被删除");
        }
        boolean liked = Boolean.TRUE.equals(data.get("liked"));
        return Result.success(liked ? "点赞成功" : "已取消点赞", data);
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
