package com.example.decoratebackservice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户意见反馈实体（user_feedback）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户意见反馈实体")
public class UserFeedback {

    @Schema(description = "主键ID", example = "1")
    private int id;

    @Schema(description = "提交用户ID", example = "1")
    private int user_id;

    @Schema(description = "反馈内容", example = "首页加载较慢")
    private String content;

    @Schema(description = "用户联系方式", example = "13800000000")
    private String contact_info;

    @Schema(description = "处理状态 0待处理1已处理", example = "0")
    private int handle_status;

    @Schema(description = "处理回复", example = "已优化")
    private String handle_remark;

    @Schema(description = "提交时间（数据库默认 CURRENT_TIMESTAMP）", example = "2026-10-28 10:00:00")
    // 统一成「yyyy-MM-dd HH:mm:ss」，前端无需再处理 ISO 的 T，管理端回传该字段时也能正确反序列化
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime create_time;

    @Schema(description = "逻辑删除 0未删1已删", example = "0")
    private int is_deleted;
}
