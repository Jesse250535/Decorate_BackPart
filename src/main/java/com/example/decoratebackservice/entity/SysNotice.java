package com.example.decoratebackservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统公告实体（sys_notice）
 * 字段严格对应数据库表 sys_notice 的列设计
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "系统公告实体")
public class SysNotice {

    @Schema(description = "主键ID", example = "1")
    private Long id;

    @Schema(description = "公告标题", example = "平台维护通知")
    private String title;

    @Schema(description = "公告详情", example = "系统将于今晚 22:00 进行维护")
    private String content;

    @Schema(description = "展示位置 1首页顶部2用户中心3后台首页", example = "1")
    private Integer show_position;

    @Schema(description = "发布时间", example = "2026-01-01T10:00:00")
    private LocalDateTime publish_time;

    @Schema(description = "失效时间", example = "2026-02-01T10:00:00")
    private LocalDateTime expire_time;

    @Schema(description = "发布状态 0草稿1已发布2已下线", example = "0")
    private Integer status;

    @Schema(description = "发布管理员ID", example = "1")
    private Long create_user_id;

    @Schema(description = "逻辑删除 0未删1已删", example = "0")
    private Integer is_deleted;
}
