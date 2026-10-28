package com.example.decoratebackservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论实体（decoration_comment）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评论实体")
public class DecorationComment {

    @Schema(description = "主键ID", example = "1")
    private int id;

    @Schema(description = "发布用户ID", example = "1")
    private int user_id;

    @Schema(description = "评论对象类型 1装修方案2装修材料", example = "1")
    private int target_type;

    @Schema(description = "关联对象ID", example = "1")
    private int target_id;

    @Schema(description = "父评论ID 0代表主评论", example = "0")
    private int parent_id;

    @Schema(description = "评论内容", example = "很赞的方案")
    private String content;

    @Schema(description = "评论图片，多图逗号分隔", example = "a.png,b.png")
    private String images;

    @Schema(description = "点赞数", example = "0")
    private int like_count;

    @Schema(description = "审核状态 0正常1违规删除", example = "0")
    private int status;

    @Schema(description = "发布时间（数据库默认 CURRENT_TIMESTAMP）", example = "2026-09-18 10:00:00")
    private LocalDateTime create_time;

    @Schema(description = "逻辑删除 0未删1已删", example = "0")
    private int is_deleted;

    /* ==================== 以下为详情页评论展示字段（非数据库列） ==================== */

    @Schema(description = "评论者昵称（展示用，非数据库字段）", example = "装修小达人")
    private String user_name;

    @Schema(description = "评论者头像地址（展示用，非数据库字段）")
    private String user_avatar;

    @Schema(description = "当前登录用户是否已点赞（展示用，非数据库字段）")
    private boolean liked;

    @Schema(description = "该评论下的回复（展示用，非数据库字段）")
    private List<DecorationComment> replies;
}
