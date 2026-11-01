package com.example.decoratebackservice.service;

import com.example.decoratebackservice.entity.ChatConversation;
import com.example.decoratebackservice.entity.ChatMessage;
import com.example.decoratebackservice.entity.DesignServiceOrder;
import com.example.decoratebackservice.entity.User;
import com.example.decoratebackservice.mapper.ChatConversationMapper;
import com.example.decoratebackservice.mapper.ChatMessageMapper;
import com.example.decoratebackservice.security.StaffRole;
import com.example.decoratebackservice.vo.ChatConversationVO;
import com.example.decoratebackservice.vo.ChatPeerVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 站内聊天业务层（业主 ↔ 设计师）
 * <p>
 * 设计要点：
 * <ul>
 *     <li>会话按（业主, 设计师）唯一，首次发消息时自动创建；</li>
 *     <li>未读数分列维护，谁读消息就清谁那一列，并在 chat_message 上标记已读；</li>
 *     <li>消息按 id 增量拉取（afterId），前端轮询只取新消息，避免重复渲染。</li>
 * </ul>
 */
@Service
public class ChatService {

    /** 发送者身份：业主 */
    public static final int ROLE_USER = 1;
    /** 发送者身份：设计师 */
    public static final int ROLE_DESIGNER = 2;

    public static final int MSG_TEXT = 1;
    public static final int MSG_IMAGE = 2;

    private static final int MAX_TEXT_LENGTH = 1000;
    private static final int SUMMARY_LENGTH = 60;

    @Autowired
    private ChatConversationMapper chatConversationMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private DesignServiceOrderService designServiceOrderService;

    /**
     * 我的会话列表（按最后消息时间倒序），并对端用户资料一并返回
     */
    public List<ChatConversationVO> listConversations(int me, int myRole) {
        List<ChatConversation> conversations = myRole == ROLE_DESIGNER
                ? chatConversationMapper.findByDesigner(me)
                : chatConversationMapper.findByUser(me);
        List<ChatConversationVO> result = new ArrayList<>(conversations.size());
        for (ChatConversation c : conversations) {
            int peerId = myRole == ROLE_DESIGNER ? c.getUser_id() : c.getDesigner_id();
            User peer = userService.findById(peerId);
            ChatConversationVO vo = new ChatConversationVO();
            vo.setId(c.getId());
            vo.setPeer_id(peerId);
            vo.setPeer_name(displayName(peer, peerId));
            vo.setPeer_avatar(peer == null ? null : peer.getAvatar());
            vo.setPeer_role(myRole == ROLE_DESIGNER ? ROLE_USER : ROLE_DESIGNER);
            vo.setLast_message(c.getLast_message());
            vo.setLast_time(c.getLast_time());
            vo.setUnread(myRole == ROLE_DESIGNER ? c.getDesigner_unread() : c.getUser_unread());
            result.add(vo);
        }
        return result;
    }

    /**
     * 可发起聊天的联系人：业主侧是全部设计师，设计师侧是自己服务单里的业主；
     * 已聊过的对端排在最前面。
     */
    public List<ChatPeerVO> listPeers(int me, int myRole) {
        int peerRole = myRole == ROLE_DESIGNER ? ROLE_USER : ROLE_DESIGNER;
        Map<Integer, ChatPeerVO> peers = new LinkedHashMap<>();

        for (ChatConversationVO c : listConversations(me, myRole)) {
            ChatPeerVO vo = new ChatPeerVO();
            vo.setId(c.getPeer_id());
            vo.setName(c.getPeer_name());
            vo.setAvatar(c.getPeer_avatar());
            vo.setRole(c.getPeer_role());
            vo.setConversed(true);
            peers.put(c.getPeer_id(), vo);
        }

        if (myRole == ROLE_DESIGNER) {
            for (DesignServiceOrder order : designServiceOrderService.findByDesigner(me)) {
                addPeer(peers, order.getUser_id(), ROLE_USER);
            }
        } else {
            for (User user : userService.findByRoleCode(StaffRole.DESIGNER)) {
                addPeer(peers, user.getId(), ROLE_DESIGNER);
            }
        }
        return new ArrayList<>(peers.values());
    }

    /**
     * 取（我, 对端）的会话，不存在则创建
     */
    @Transactional
    public ChatConversation getOrCreate(int me, int myRole, int peerId) {
        if (peerId <= 0) {
            throw new IllegalArgumentException("请选择聊天对象");
        }
        if (peerId == me) {
            throw new IllegalArgumentException("不能和自己聊天");
        }
        User peer = userService.findById(peerId);
        if (peer == null) {
            throw new IllegalArgumentException("聊天对象不存在");
        }
        int userId = myRole == ROLE_DESIGNER ? peerId : me;
        int designerId = myRole == ROLE_DESIGNER ? me : peerId;
        ChatConversation exist = chatConversationMapper.findByUserAndDesigner(userId, designerId);
        if (exist != null) {
            return exist;
        }
        ChatConversation conversation = new ChatConversation();
        conversation.setUser_id(userId);
        conversation.setDesigner_id(designerId);
        conversation.setLast_message(null);
        conversation.setLast_time(LocalDateTime.now());
        conversation.setUser_unread(0);
        conversation.setDesigner_unread(0);
        conversation.setStatus(1);
        try {
            chatConversationMapper.insert(conversation);
        } catch (Exception e) {
            // 并发首聊：唯一索引冲突时回查已存在的会话
            ChatConversation again = chatConversationMapper.findByUserAndDesigner(userId, designerId);
            if (again != null) {
                return again;
            }
            throw e;
        }
        return chatConversationMapper.findById(conversation.getId());
    }

    /**
     * 发消息：文字或图片至少有一项
     */
    @Transactional
    public ChatMessage send(int me, int myRole, int peerId, String content, String imageUrl) {
        String text = content == null ? "" : content.trim();
        String image = imageUrl == null ? "" : imageUrl.trim();
        if (text.isEmpty() && image.isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        if (text.length() > MAX_TEXT_LENGTH) {
            throw new IllegalArgumentException("单条消息不能超过 " + MAX_TEXT_LENGTH + " 字");
        }
        ChatConversation conversation = getOrCreate(me, myRole, peerId);

        ChatMessage message = new ChatMessage();
        message.setConversation_id(conversation.getId());
        message.setSender_id(me);
        message.setSender_role(myRole);
        message.setMsg_type(image.isEmpty() ? MSG_TEXT : MSG_IMAGE);
        message.setContent(text.isEmpty() ? null : text);
        message.setImage_url(image.isEmpty() ? null : image);
        message.setIs_read(0);
        message.setIs_deleted(0);
        chatMessageMapper.insert(message);

        String summary = text.isEmpty() ? "[图片]" : text;
        if (summary.length() > SUMMARY_LENGTH) {
            summary = summary.substring(0, SUMMARY_LENGTH) + "…";
        }
        chatConversationMapper.updateLast(conversation.getId(), summary, LocalDateTime.now());
        if (myRole == ROLE_DESIGNER) {
            chatConversationMapper.incrUserUnread(conversation.getId());
        } else {
            chatConversationMapper.incrDesignerUnread(conversation.getId());
        }
        return chatMessageMapper.findById(message.getId());
    }

    /**
     * 拉取消息；afterId > 0 时只返回比它更新的消息（轮询用）
     */
    public List<ChatMessage> listMessages(long conversationId, int me, int myRole, long afterId) {
        ChatConversation conversation = requireConversation(conversationId, me, myRole);
        return chatMessageMapper.findByConversation(conversation.getId(), Math.max(0, afterId));
    }

    /**
     * 标记会话已读：清自己的未读数，并把对方发来的消息置为已读
     */
    @Transactional
    public void markRead(long conversationId, int me, int myRole) {
        ChatConversation conversation = requireConversation(conversationId, me, myRole);
        chatMessageMapper.markRead(conversation.getId(), myRole);
        if (myRole == ROLE_DESIGNER) {
            chatConversationMapper.clearDesignerUnread(conversation.getId());
        } else {
            chatConversationMapper.clearUserUnread(conversation.getId());
        }
    }

    /**
     * 我的总未读数（首页入口的小红点用）
     */
    public int totalUnread(int me, int myRole) {
        List<ChatConversation> conversations = myRole == ROLE_DESIGNER
                ? chatConversationMapper.findByDesigner(me)
                : chatConversationMapper.findByUser(me);
        int sum = 0;
        for (ChatConversation c : conversations) {
            sum += myRole == ROLE_DESIGNER ? c.getDesigner_unread() : c.getUser_unread();
        }
        return sum;
    }

    private ChatConversation requireConversation(long conversationId, int me, int myRole) {
        ChatConversation conversation = chatConversationMapper.findById(conversationId);
        if (conversation == null) {
            throw new IllegalArgumentException("会话不存在");
        }
        boolean mine = myRole == ROLE_DESIGNER
                ? conversation.getDesigner_id() == me
                : conversation.getUser_id() == me;
        if (!mine) {
            throw new SecurityException("无权访问该会话");
        }
        return conversation;
    }

    private void addPeer(Map<Integer, ChatPeerVO> peers, int userId, int role) {
        if (peers.containsKey(userId)) {
            return;
        }
        User user = userService.findById(userId);
        if (user == null) {
            return;
        }
        ChatPeerVO vo = new ChatPeerVO();
        vo.setId(userId);
        vo.setName(displayName(user, userId));
        vo.setAvatar(user.getAvatar());
        vo.setRole(role);
        vo.setConversed(false);
        peers.put(userId, vo);
    }

    private String displayName(User user, int fallbackId) {
        if (user == null) {
            return "用户" + fallbackId;
        }
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }
        return user.getUsername() == null ? "用户" + fallbackId : user.getUsername();
    }
}
