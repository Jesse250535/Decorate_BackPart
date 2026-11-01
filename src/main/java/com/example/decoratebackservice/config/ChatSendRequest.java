package com.example.decoratebackservice.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 发送聊天消息的请求体：文字与图片至少传一项。
 */
@Data
@Schema(description = "发送聊天消息请求")
public class ChatSendRequest {

    @Schema(description = "对端用户ID（业主填设计师ID，设计师填业主ID）", example = "7")
    private int peer_id;

    @Schema(description = "文字内容")
    private String content;

    @Schema(description = "图片地址（先用 /file/upload 上传拿 url）")
    private String image_url;
}
