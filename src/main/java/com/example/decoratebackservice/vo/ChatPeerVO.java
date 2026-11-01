package com.example.decoratebackservice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 可发起聊天的联系人（业主侧是设计师，设计师侧是自己服务单里的业主）。
 */
@Data
@Schema(description = "聊天联系人")
public class ChatPeerVO {

    @Schema(description = "用户ID")
    private int id;

    @Schema(description = "昵称")
    private String name;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "身份 1业主 2设计师")
    private int role;

    @Schema(description = "是否已有会话")
    private boolean conversed;
}
