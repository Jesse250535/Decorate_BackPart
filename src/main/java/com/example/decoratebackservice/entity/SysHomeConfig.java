package com.example.decoratebackservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 首页配置实体（sys_home_config）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "首页配置实体")
public class SysHomeConfig {

    @Schema(description = "主键ID", example = "1")
    private int id;

    @Schema(description = "配置类型 1轮播图2精选成功案例图3首页推荐位", example = "1")
    private int config_type;

    @Schema(description = "图片地址", example = "banner.png")
    private String image_url;

    @Schema(description = "点击跳转链接", example = "/plan/1")
    private String jump_link;

    @Schema(description = "排序权重", example = "0")
    private int sort;

    @Schema(description = "展示状态 0隐藏1显示", example = "1")
    private int status;

    @Schema(description = "逻辑删除 0未删1已删", example = "0")
    private int is_deleted;
}
