package com.example.decoratebackservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 分期付款节点（construction_payment_node）
 * <p>
 * 签订合同后按报价总额比例生成：定金 → 瓷砖完成 → 全屋定制完成 → 保洁完成尾款。
 * 中间两期必须等对应施工阶段业主验收通过才允许支付（trigger_stage_code）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分期付款节点")
public class ConstructionPaymentNode {

    @Schema(description = "主键ID")
    private int id;

    @Schema(description = "设计服务单ID")
    private int service_id;

    @Schema(description = "节点编码 DEPOSIT/TILING/CUSTOM/FINAL")
    private String node_code;

    @Schema(description = "节点名称")
    private String node_name;

    @Schema(description = "占总价比例(%)")
    private BigDecimal ratio;

    @Schema(description = "本期应付金额")
    private BigDecimal amount;

    @Schema(description = "前置阶段编码，空表示签约即付")
    private String trigger_stage_code;

    @Schema(description = "0待支付1已支付2已退款")
    private int pay_status;

    @Schema(description = "支付时间")
    private LocalDateTime pay_time;

    @Schema(description = "支付流水号")
    private String trade_no;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime create_time;

    @Schema(description = "更新时间")
    private LocalDateTime update_time;
}
