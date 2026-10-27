package com.example.decoratebackservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 分类标签实体（decoration_category）
 * <p>
 * 项目原本没有该表的 Java 映射，库管上传材料时需要选择「材料分类」，
 * 因此补充一份最小实现，只用于下拉选项，不改动任何既有代码。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分类标签实体")
public class DecorationCategory {

    @Schema(description = "主键ID", example = "3")
    private int id;

    @Schema(description = "分类名称", example = "瓷砖石材")
    private String category_name;

    @Schema(description = "分类类型 1装修方案分类 2装修材料分类", example = "2")
    private int category_type;

    @Schema(description = "父分类ID", example = "0")
    private int parent_id;

    @Schema(description = "排序权重", example = "0")
    private int sort;

    @Schema(description = "是否热门 0否1是", example = "0")
    private int is_hot;

    @Schema(description = "创建时间")
    private LocalDateTime create_time;

    @Schema(description = "逻辑删除 0未删1已删", example = "0")
    private int is_deleted;
}
