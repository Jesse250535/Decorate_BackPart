package com.example.decoratebackservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户点赞收藏实体（user_like_collect）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户点赞收藏实体")
public class UserLikeCollect {

    @Schema(description = "主键ID", example = "1")
    private int id;

    @Schema(description = "用户ID", example = "1")
    private int user_id;

    @Schema(description = "操作类型 1点赞2收藏", example = "1")
    private int operate_type;

    @Schema(description = "对象类型 1装修方案2装修材料3评论", example = "1")
    private int target_type;

    @Schema(description = "关联对象ID", example = "1")
    private int target_id;

    @Schema(description = "逻辑删除 0未删1已删", example = "0")
    private int is_deleted;
}
