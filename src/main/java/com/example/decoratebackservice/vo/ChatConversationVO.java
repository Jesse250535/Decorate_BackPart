package com.example.decoratebackservice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话列表项：把会话表 + 对端用户资料拼在一起，前端直接渲染列表即可。
 */
@Data
@Schema(description = "会话列表项")
public class ChatConversationVO {

    @Schema(description = "会话ID")
    private long id;

    @Schema(description = "对端用户ID（业主看到的是设计师，设计师看到的是业主）")
    private int peer_id;

    @Schema(description = "对端昵称")
    private String peer_name;

    @Schema(description = "对端头像")
    private String peer_avatar;

    @Schema(description = "对端身份 1业主 2设计师")
    private int peer_role;

    @Schema(description = "最后一条消息摘要")
    private String last_message;

    @Schema(description = "最后消息时间")
    private LocalDateTime last_time;

    @Schema(description = "我的未读数")
    private int unread;
}
