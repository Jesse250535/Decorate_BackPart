package com.example.decoratebackservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付交易流水（payment_record）
 * <p>
 * 一次「业主点击支付某个付款节点」= 一条交易记录。
 * 付款节点表只关心「这期付没付」，交易流水负责对接支付宝：
 * out_trade_no 是商户订单号，trade_no 是支付宝交易号，两者在支付回调里互相校验。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "支付交易流水")
public class PaymentRecord {

    @Schema(description = "主键ID")
    private int id;

    @Schema(description = "商户订单号（交给支付宝的 out_trade_no）")
    private String out_trade_no;

    @Schema(description = "设计服务单ID")
    private int service_id;

    @Schema(description = "对应的付款节点ID")
    private int node_id;

    @Schema(description = "付款业主ID")
    private int user_id;

    @Schema(description = "应付金额（单位：元）")
    private BigDecimal amount;

    @Schema(description = "商品标题，展示在支付宝收银台")
    private String subject;

    @Schema(description = "支付宝交易号")
    private String trade_no;

    @Schema(description = "支付渠道 alipay/mock")
    private String pay_mode;

    @Schema(description = "0待支付 1已支付 2已关闭")
    private int status;

    @Schema(description = "回调时间")
    private LocalDateTime notify_time;

    @Schema(description = "创建时间")
    private LocalDateTime create_time;
}
