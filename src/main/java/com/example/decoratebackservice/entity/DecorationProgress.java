package com.example.decoratebackservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 装修进度跟踪实体（decoration_progress）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "装修进度跟踪实体")
public class DecorationProgress {

    @Schema(description = "主键ID", example = "1")
    private int id;

    @Schema(description = "关联订单ID", example = "1")
    private int order_id;

    @Schema(description = "装修状态 1待开工2施工中3待竣工验收4已完成5施工异常", example = "1")
    private int progress_status;

    @Schema(description = "当前进度描述", example = "水电改造完成")
    private String status_desc;

    @Schema(description = "现场实拍图，多图逗号分隔", example = "a.png,b.png")
    private String progress_images;

    @Schema(description = "上报施工人员ID", example = "1")
    private int report_user_id;

    @Schema(description = "逻辑删除 0未删1已删", example = "0")
    private int is_deleted;
}
