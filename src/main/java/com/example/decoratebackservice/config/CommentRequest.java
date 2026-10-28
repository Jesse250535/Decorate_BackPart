package com.example.decoratebackservice.config;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 发表评论 / 回复的请求体。
 * <p>
 * 只暴露前端真正需要的字段：user_id、like_count、status 等由后端决定，
 * 不接受前端传入，避免刷赞或伪造评论者。
 */
@Data
@Schema(description = "发表评论请求")
public class CommentRequest {

    @Schema(description = "评论对象类型 1装修方案 2装修材料", example = "1")
    private Integer target_type;

    @Schema(description = "评论对象ID", example = "1")
    private Integer target_id;

    @Schema(description = "父评论ID，0或不传表示主评论，否则为回复", example = "0")
    private Integer parent_id;

    @Schema(description = "评论内容", example = "整体落地效果非常满意")
    private String content;
}
