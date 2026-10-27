package com.example.decoratebackservice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 「我的装修进度」视图对象：订单信息 + 该订单最新一条进度
 * <p>
 * 仅用于前台用户查询自己的装修进度，不改动原有 DecorationProgress 实体与接口。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "我的装修进度")
public class OrderProgressVO {

    @Schema(description = "订单主键", example = "1")
    private int order_id;

    @Schema(description = "订单编号", example = "DO202609010001")
    private String order_no;

    @Schema(description = "订单类型 1购买装修方案2购买装修材料", example = "1")
    private int order_type;

    @Schema(description = "关联资源ID（方案/材料主键）", example = "1")
    private int target_id;

    @Schema(description = "订单状态 0待支付1已支付2已完成3已退款", example = "1")
    private int order_status;

    @Schema(description = "是否可查看装修进度 1可查看 0不可查看", example = "1")
    private int can_view;

    @Schema(description = "进度主键，不可查看时为空")
    private Integer progress_id;

    @Schema(description = "装修状态 1待开工2施工中3待竣工验收4已完成5施工异常，不可查看时为空")
    private Integer progress_status;

    @Schema(description = "当前进度描述，不可查看时为空")
    private String status_desc;

    @Schema(description = "现场实拍图，多图逗号分隔，不可查看时为空")
    private String progress_images;

    @Schema(description = "上报时间，不可查看时为空")
    private LocalDateTime report_time;

    @Schema(description = "提示文案：不可查看的原因 / 等待上报 / 无进度")
    private String message;
}
