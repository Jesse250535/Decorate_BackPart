package com.example.decoratebackservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 装修合同（design_contract）
 * <p>
 * 报价确认后由项目经理签订，合同金额取报价总额；
 * 签订成功即生成施工阶段计划与分期付款节点。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "装修合同")
public class DesignContract {

    @Schema(description = "主键ID")
    private int id;

    @Schema(description = "合同编号")
    private String contract_no;

    @Schema(description = "设计服务单ID")
    private int service_id;

    @Schema(description = "业主ID")
    private int user_id;

    @Schema(description = "设计师ID")
    private int designer_id;

    @Schema(description = "项目经理ID")
    private int manager_id;

    @Schema(description = "合同金额")
    private BigDecimal contract_amount;

    @Schema(description = "装修方式 1硬装2软装3整装")
    private int decoration_mode;

    @Schema(description = "0待签1已签2已作废")
    private int sign_status;

    @Schema(description = "签订时间")
    private LocalDateTime sign_date;

    @Schema(description = "计划开工日期")
    private LocalDate start_date;

    @Schema(description = "合同附件地址")
    private String file_url;

    @Schema(description = "业主签名图片地址")
    private String signature_url;

    @Schema(description = "首付款金额（(合同金额-定金)×15%）")
    private BigDecimal first_payment_amount;

    @Schema(description = "首付款支付时间")
    private LocalDateTime first_payment_time;

    @Schema(description = "合同回执地址")
    private String receipt_url;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime create_time;

    @Schema(description = "更新时间")
    private LocalDateTime update_time;

    @Schema(description = "逻辑删除 0未删1已删")
    private int is_deleted;
}
