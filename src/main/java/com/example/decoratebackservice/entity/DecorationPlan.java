package com.example.decoratebackservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 装修方案实体（decoration_plan）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "装修方案实体")
public class DecorationPlan {

    @Schema(description = "主键ID", example = "1")
    private int id;

    @Schema(description = "方案名称", example = "现代简约三居室")
    private String plan_name;

    @Schema(description = "封面图", example = "cover.png")
    private String cover_image;

    @Schema(description = "实景案例图集，多图逗号分隔", example = "a.png,b.png")
    private String detail_images;

    @Schema(description = "方案价格", example = "19999.00")
    private BigDecimal price;

    @Schema(description = "适用最小面积(平米)", example = "60")
    private int area_min;

    @Schema(description = "适用最大面积(平米)", example = "120")
    private int area_max;

    @Schema(description = "风格描述", example = "现代简约")
    private String style;

    @Schema(description = "方案详细描述", example = "方案详情")
    private String description;

    @Schema(description = "关联分类ID", example = "1")
    private int category_id;

    @Schema(description = "上传者用户ID", example = "1")
    private int create_user_id;

    @Schema(description = "审核状态 0待审核1通过2驳回", example = "0")
    private int audit_status;

    @Schema(description = "审核驳回原因", example = "图片不清晰")
    private String audit_remark;

    @Schema(description = "购买销量", example = "0")
    private int sales_count;

    @Schema(description = "浏览次数", example = "0")
    private int view_count;

    @Schema(description = "收藏次数", example = "0")
    private int collect_count;

    @Schema(description = "上架状态 0下架1上架", example = "0")
    private int status;

    @Schema(description = "逻辑删除 0未删1已删", example = "0")
    private int is_deleted;
}
