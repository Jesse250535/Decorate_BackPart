package com.example.decoratebackservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 业主 - 设计师 会话（chat_conversation）
 * <p>
 * 一对（用户, 设计师）只有一条会话，由唯一索引 uk_chat_user_designer 保证；
 * 未读数分两列维护，读取消息时按当前身份清零自己那一列。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "聊天会话")
public class ChatConversation {

    @Schema(description = "主键ID")
    private long id;

    @Schema(description = "业主 sys_user.id")
    private int user_id;

    @Schema(description = "设计师 sys_user.id")
    private int designer_id;

    @Schema(description = "围绕的方案ID（可空）")
    private Integer plan_id;

    @Schema(description = "最后一条消息摘要")
    private String last_message;

    @Schema(description = "最后消息时间")
    private LocalDateTime last_time;

    @Schema(description = "业主未读数")
    private int user_unread;

    @Schema(description = "设计师未读数")
    private int designer_unread;

    @Schema(description = "状态 0关闭 1正常")
    private int status;

    @Schema(description = "创建时间")
    private LocalDateTime create_time;

    @Schema(description = "更新时间")
    private LocalDateTime update_time;
}
