package com.example.decoratebackservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 施工阶段（construction_stage）
 * <p>
 * 每个阶段记录计划 / 实际起止日期、是否按时、业主验收结论与返工工期：
 * 验收不通过 → status=4 返工中，登记 rework_days 与返工起止日期，
 * 返工完成后重新提交验收。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "施工阶段进度")
public class ConstructionStage {

    @Schema(description = "主键ID")
    private int id;

    @Schema(description = "设计服务单ID")
    private int service_id;

    @Schema(description = "阶段编码")
    private String stage_code;

    @Schema(description = "阶段名称")
    private String stage_name;

    @Schema(description = "1硬装 2软装")
    private int stage_type;

    @Schema(description = "施工顺序")
    private int seq;

    @Schema(description = "计划开始日期")
    private LocalDate plan_start_date;

    @Schema(description = "计划结束日期")
    private LocalDate plan_end_date;

    @Schema(description = "实际开始日期")
    private LocalDate actual_start_date;

    @Schema(description = "实际结束日期")
    private LocalDate actual_end_date;

    @Schema(description = "计划工期(天)")
    private int plan_days;

    @Schema(description = "实际工期(天)")
    private int actual_days;

    @Schema(description = "超期天数")
    private int delay_days;

    @Schema(description = "是否按时 0未判定1按时2超期")
    private int on_time;

    @Schema(description = "状态 0未开始1进行中2已完工3已验收4返工中")
    private int status;

    @Schema(description = "业主验收 0待验收1通过2不通过")
    private int accept_status;

    @Schema(description = "验收业主ID")
    private Integer accept_user_id;

    @Schema(description = "验收时间")
    private LocalDateTime accept_time;

    @Schema(description = "验收意见/不通过原因")
    private String accept_remark;

    @Schema(description = "返工状态 0无需返工1返工中2返工完成")
    private int rework_status;

    @Schema(description = "返工工期(天)")
    private int rework_days;

    @Schema(description = "返工开始日期")
    private LocalDate rework_start_date;

    @Schema(description = "返工完成日期")
    private LocalDate rework_end_date;

    @Schema(description = "返工次数")
    private int rework_count;

    @Schema(description = "负责项目经理ID")
    private Integer manager_id;

    @Schema(description = "现场实拍图，多图逗号分隔")
    private String images;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime create_time;

    @Schema(description = "更新时间")
    private LocalDateTime update_time;

    @Schema(description = "逻辑删除 0未删1已删")
    private int is_deleted;
}
