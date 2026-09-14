package com.example.decoratebackservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 系统访问统计实体（sys_visit_stat）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "系统访问统计实体")
public class SysVisitStat {

    @Schema(description = "主键ID", example = "1")
    private int id;

    @Schema(description = "统计日期", example = "2026-09-14")
    private LocalDate stat_date;

    @Schema(description = "页面浏览量", example = "1000")
    private int pv;

    @Schema(description = "独立访客数", example = "200")
    private int uv;

    @Schema(description = "新增注册用户数", example = "10")
    private int new_user_count;

    @Schema(description = "新增上传方案数", example = "2")
    private int create_plan_count;

    @Schema(description = "新增上传材料数", example = "3")
    private int create_material_count;

    @Schema(description = "完成订单数", example = "5")
    private int finish_order_count;
}
