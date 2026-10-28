package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.entity.UserFeedback;
import com.example.decoratebackservice.service.UserFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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

    /* ==================== 前台用户：提交 / 查看自己的反馈 ==================== */

    /**
     * 提交意见反馈。
     * <p>
     * user_id 取自登录会话，前端传入的 user_id / handle_status 一律忽略，
     * 保证「谁提的反馈就记在谁名下」，也避免用户把状态直接改成已处理。
     */
    @Operation(summary = "提交意见反馈（user_id 取自当前登录用户）")
    @PostMapping("/my")
    public Result<UserFeedback> submitMy(@RequestBody UserFeedback body, Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录后再提交反馈");
        }
        if (body == null || body.getContent() == null || body.getContent().isBlank()) {
            return Result.validateFailed("反馈内容不能为空");
        }
        if (body.getContent().trim().length() > UserFeedbackService.MAX_CONTENT_LENGTH) {
            return Result.validateFailed("反馈内容不能超过 " + UserFeedbackService.MAX_CONTENT_LENGTH + " 字");
        }
        if (body.getContact_info() != null
                && body.getContact_info().trim().length() > UserFeedbackService.MAX_CONTACT_LENGTH) {
            return Result.validateFailed("联系方式不能超过 " + UserFeedbackService.MAX_CONTACT_LENGTH + " 个字符");
        }
        UserFeedback saved = userFeedbackService.submit(userId, body.getContent(), body.getContact_info());
        if (saved == null) {
            return Result.error(400, "提交失败，请检查反馈内容");
        }
        return Result.success("反馈已提交，感谢你的建议", saved);
    }

    @Operation(summary = "查询我提交过的意见反馈（含处理状态与回复）")
    @GetMapping("/my")
    public Result<List<UserFeedback>> myFeedbacks(Authentication authentication) {
        Integer userId = currentUserId(authentication);
        if (userId == null) {
            return Result.unauthorized("请先登录后再查看反馈记录");
        }
        List<UserFeedback> list = userFeedbackService.findMyFeedbacks(userId);
        if (list.isEmpty()) {
            return Result.success("你还没有提交过反馈", list);
        }
        return Result.success(list);
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
        return userFeedbackService.findUserIdByUsername(authentication.getName());
    }
}
