package com.example.decoratebackservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天消息（chat_message）
 * <p>
 * sender_role 区分发送方：1 业主、2 设计师，前端据此决定消息气泡靠左还是靠右。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天消息")
public class ChatMessage {

    @Schema(description = "主键ID")
    private long id;

    @Schema(description = "会话ID")
    private long conversation_id;

    @Schema(description = "发送者 sys_user.id")
    private int sender_id;

    @Schema(description = "发送者身份 1业主 2设计师")
    private int sender_role;

    @Schema(description = "消息类型 1文字 2图片")
    private int msg_type;

    @Schema(description = "文字内容")
    private String content;

    @Schema(description = "图片地址")
    private String image_url;

    @Schema(description = "卡片关联对象ID")
    private Long target_id;

    @Schema(description = "对方是否已读 0未读 1已读")
    private int is_read;

    @Schema(description = "发送时间")
    private LocalDateTime create_time;

    @Schema(description = "逻辑删除 0未删 1已删")
    private int is_deleted;
}
