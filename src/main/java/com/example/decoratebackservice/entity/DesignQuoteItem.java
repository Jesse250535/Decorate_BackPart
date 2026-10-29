package com.example.decoratebackservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报价单明细（design_quote_item）
 * <p>
 * 一条明细归属一个硬装 / 软装阶段，便于业主看清「钱花在哪个阶段」，
 * 也便于确认报价后按阶段生成施工计划。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "报价单明细")
public class DesignQuoteItem {

    @Schema(description = "主键ID")
    private int id;

    @Schema(description = "报价单ID")
    private int quote_id;

    @Schema(description = "1硬装 2软装")
    private int item_type;

    @Schema(description = "归属施工阶段编码")
    private String stage_code;

    @Schema(description = "项目名称")
    private String item_name;

    @Schema(description = "规格/工艺说明")
    private String spec;

    @Schema(description = "单位")
    private String unit;

    @Schema(description = "数量")
    private BigDecimal quantity;

    @Schema(description = "单价")
    private BigDecimal unit_price;

    @Schema(description = "小计")
    private BigDecimal amount;

    @Schema(description = "品牌/型号")
    private String brand;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "排序")
    private int sort;

    @Schema(description = "创建时间")
    private LocalDateTime create_time;

    @Schema(description = "逻辑删除 0未删1已删")
    private int is_deleted;
}
