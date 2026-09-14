package com.example.decoratebackservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体（decoration_order）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单实体")
public class DecorationOrder {

    @Schema(description = "主键ID", example = "1")
    private int id;

    @Schema(description = "订单编号", example = "ORD202601010001")
    private String order_no;

    @Schema(description = "下单用户ID", example = "1")
    private int user_id;

    @Schema(description = "订单类型 1购买装修方案2购买装修材料", example = "1")
    private int order_type;

    @Schema(description = "关联资源ID", example = "1")
    private int target_id;

    @Schema(description = "订单总金额", example = "19999.00")
    private BigDecimal total_amount;

    @Schema(description = "订单状态 0待支付1已支付2已完成3已退款", example = "0")
    private int order_status;

    @Schema(description = "合同状态 0未签订1已签订", example = "0")
    private int contract_status;

    @Schema(description = "退款状态 0无退款1待审核2已退款3驳回", example = "0")
    private int refund_status;

    @Schema(description = "用户订单备注", example = "尽快发货")
    private String remark;

    @Schema(description = "支付时间")
    private LocalDateTime pay_time;

    @Schema(description = "合同签订时间")
    private LocalDateTime contract_time;

    @Schema(description = "订单完成时间")
    private LocalDateTime finish_time;

    @Schema(description = "逻辑删除 0未删1已删", example = "0")
    private int is_deleted;
}
