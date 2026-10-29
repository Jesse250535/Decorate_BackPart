package com.example.decoratebackservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 装修报价单（design_quote）
 * <p>
 * 硬装、软装金额分开统计，业主可以只做硬装、只做软装或整装；
 * 报价总额是分期付款节点的计算基数。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "装修报价单")
public class DesignQuote {

    @Schema(description = "主键ID")
    private int id;

    @Schema(description = "报价单号")
    private String quote_no;

    @Schema(description = "设计服务单ID")
    private int service_id;

    @Schema(description = "业主ID")
    private int user_id;

    @Schema(description = "报价设计师ID")
    private int designer_id;

    @Schema(description = "硬装合计")
    private BigDecimal hard_amount;

    @Schema(description = "软装合计")
    private BigDecimal soft_amount;

    @Schema(description = "报价总额")
    private BigDecimal total_amount;

    @Schema(description = "状态 0草稿1待业主确认2已确认3已作废")
    private int status;

    @Schema(description = "报价说明")
    private String remark;

    @Schema(description = "业主确认时间")
    private LocalDateTime confirm_time;

    @Schema(description = "创建时间")
    private LocalDateTime create_time;

    @Schema(description = "更新时间")
    private LocalDateTime update_time;

    @Schema(description = "逻辑删除 0未删1已删")
    private int is_deleted;
}
