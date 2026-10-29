package com.example.decoratebackservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设计服务单（design_service_order）
 * <p>
 * 承载「业主找设计师 → 免费量房 → 平面图 → 定金 → 效果图 → 报价 → 合同 → 施工」整条主线，
 * 状态含义见 {@code 6_design_flow.sql} 头部状态机。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "设计服务单")
public class DesignServiceOrder {

    @Schema(description = "主键ID")
    private int id;

    @Schema(description = "设计服务单号")
    private String service_no;

    @Schema(description = "业主ID")
    private int user_id;

    @Schema(description = "设计师ID")
    private int designer_id;

    @Schema(description = "项目经理ID")
    private Integer manager_id;

    @Schema(description = "关联方案ID")
    private Integer plan_id;

    @Schema(description = "装修方式 1硬装2软装3整装")
    private int decoration_mode;

    @Schema(description = "户型")
    private String house_type;

    @Schema(description = "建筑面积(平米)")
    private BigDecimal house_area;

    @Schema(description = "项目地址")
    private String address;

    @Schema(description = "期望风格")
    private String style;

    @Schema(description = "预算(元)")
    private BigDecimal budget;

    @Schema(description = "期望量房时间")
    private LocalDateTime expect_date;

    @Schema(description = "需求描述")
    private String requirement;

    @Schema(description = "状态 0待接单1待量房2已量房3平面图待确认4平面图已确认5已付定金6效果图已出7已报价8报价已确认9已签合同10施工中11已完工12已完成13已取消")
    private int status;

    @Schema(description = "免费量房完成时间")
    private LocalDateTime measure_date;

    @Schema(description = "平面图提交时间")
    private LocalDateTime plan_drawing_time;

    @Schema(description = "平面设计图地址，多图逗号分隔")
    private String plan_drawings;

    @Schema(description = "业主确认平面图时间")
    private LocalDateTime plan_confirm_time;

    @Schema(description = "定金金额")
    private BigDecimal deposit_amount;

    @Schema(description = "定金支付时间")
    private LocalDateTime deposit_time;

    @Schema(description = "效果图提交时间")
    private LocalDateTime effect_drawing_time;

    @Schema(description = "效果图地址，多图逗号分隔")
    private String effect_drawings;

    @Schema(description = "当前生效报价单ID")
    private Integer quote_id;

    @Schema(description = "报价提交时间")
    private LocalDateTime quote_time;

    @Schema(description = "合同ID")
    private Integer contract_id;

    @Schema(description = "合同签订时间")
    private LocalDateTime contract_time;

    @Schema(description = "计划开工日期")
    private LocalDate start_date;

    @Schema(description = "整体完工时间")
    private LocalDateTime finish_time;

    @Schema(description = "取消原因")
    private String cancel_reason;

    @Schema(description = "创建时间")
    private LocalDateTime create_time;

    @Schema(description = "更新时间")
    private LocalDateTime update_time;

    @Schema(description = "逻辑删除 0未删1已删")
    private int is_deleted;
}
