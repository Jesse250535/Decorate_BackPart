package com.example.decoratebackservice.controller;

import com.example.decoratebackservice.common.Result;
import com.example.decoratebackservice.config.ChatSendRequest;
import com.example.decoratebackservice.entity.ChatMessage;
import com.example.decoratebackservice.entity.User;
import com.example.decoratebackservice.security.StaffRole;
import com.example.decoratebackservice.service.ChatService;
import com.example.decoratebackservice.service.UserService;
import com.example.decoratebackservice.vo.ChatConversationVO;
import com.example.decoratebackservice.vo.ChatPeerVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 站内聊天接口（业主 ↔ 设计师）
 * <p>
 * 业主（ROLE_USER）与设计师（ROLE_DESIGNER）共用同一套接口，
 * 通过会话里的 user_id / designer_id 判定当前用户是哪一方；
 * 数据表 chat_conversation / chat_message 见 5_designer_upgrade.sql。
 */
@Tag(name = "站内聊天接口")
@RestController
@RequestMapping("/chat")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @Operation(summary = "我的会话列表")
    @GetMapping("/conversations")
    public Result<List<ChatConversationVO>> conversations(Authentication authentication) {
        Integer me = currentUserId(authentication);
        if (me == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success(chatService.listConversations(me, chatRole(authentication)));
    }

    @Operation(summary = "可聊天联系人")
    @GetMapping("/peers")
    public Result<List<ChatPeerVO>> peers(Authentication authentication) {
        Integer me = currentUserId(authentication);
        if (me == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success(chatService.listPeers(me, chatRole(authentication)));
    }

    @Operation(summary = "拉取会话消息（afterId 为增量游标）")
    @GetMapping("/messages/{id}")
    public Result<List<ChatMessage>> messages(@PathVariable long id,
                                              @RequestParam(defaultValue = "0") long afterId,
                                              Authentication authentication) {
        Integer me = currentUserId(authentication);
        if (me == null) {
            return Result.unauthorized("请先登录");
        }
        try {
            return Result.success(chatService.listMessages(id, me, chatRole(authentication), afterId));
        } catch (SecurityException e) {
            return Result.forbidden(e.getMessage());
        }
    }

    @Operation(summary = "发送消息")
    @PostMapping("/send")
    public Result<ChatMessage> send(@RequestBody ChatSendRequest body, Authentication authentication) {
        Integer me = currentUserId(authentication);
        if (me == null) {
            return Result.unauthorized("请先登录");
        }
        ChatMessage message = chatService.send(me, chatRole(authentication),
                body.getPeer_id(), body.getContent(), body.getImage_url());
        return Result.success("发送成功", message);
    }

    @Operation(summary = "标记会话已读")
    @PostMapping("/read/{id}")
    public Result<Void> read(@PathVariable long id, Authentication authentication) {
        Integer me = currentUserId(authentication);
        if (me == null) {
            return Result.unauthorized("请先登录");
        }
        try {
            chatService.markRead(id, me, chatRole(authentication));
            return Result.success();
        } catch (SecurityException e) {
            return Result.forbidden(e.getMessage());
        }
    }

    @Operation(summary = "我的总未读数")
    @GetMapping("/unread")
    public Result<Integer> unread(Authentication authentication) {
        Integer me = currentUserId(authentication);
        if (me == null) {
            return Result.unauthorized("请先登录");
        }
        return Result.success(chatService.totalUnread(me, chatRole(authentication)));
    }

    /**
     * 取当前登录用户 id（业主与员工通用）
     */
    private Integer currentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        User user = userService.findByUsernameForStaff(authentication.getName());
        return user == null ? null : user.getId();
    }

    /**
     * 聊天身份：设计师为 2，其余（业主）为 1
     */
    private int chatRole(Authentication authentication) {
        return StaffRole.isDesigner(authentication) ? ChatService.ROLE_DESIGNER : ChatService.ROLE_USER;
    }
}
