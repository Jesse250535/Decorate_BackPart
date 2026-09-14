package com.example.decoratebackservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 装修材料实体（decoration_material）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "装修材料实体")
public class DecorationMaterial {

    @Schema(description = "主键ID", example = "1")
    private int id;

    @Schema(description = "材料名称", example = "实木地板")
    private String material_name;

    @Schema(description = "外观图", example = "material.png")
    private String cover_image;

    @Schema(description = "单价", example = "199.00")
    private BigDecimal price;

    @Schema(description = "长度参数(毫米)", example = "1200.00")
    private BigDecimal spec_long;

    @Schema(description = "宽度参数(毫米)", example = "200.00")
    private BigDecimal spec_width;

    @Schema(description = "高度参数(毫米)", example = "18.00")
    private BigDecimal spec_height;

    @Schema(description = "计量单位", example = "块")
    private String unit;

    @Schema(description = "库存数量", example = "100")
    private int stock;

    @Schema(description = "材料参数详情", example = "环保实木")
    private String description;

    @Schema(description = "关联分类ID", example = "1")
    private int category_id;

    @Schema(description = "关联供应商ID", example = "1")
    private int supplier_id;

    @Schema(description = "审核状态 0待审核1通过2驳回", example = "0")
    private int audit_status;

    @Schema(description = "审核驳回原因", example = "参数不全")
    private String audit_remark;

    @Schema(description = "销量", example = "0")
    private int sales_count;

    @Schema(description = "上架状态 0下架1上架", example = "0")
    private int status;

    @Schema(description = "逻辑删除 0未删1已删", example = "0")
    private int is_deleted;
}
