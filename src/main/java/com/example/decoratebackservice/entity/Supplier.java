package com.example.decoratebackservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 材料供应商实体（supplier）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "材料供应商实体")
public class Supplier {

    @Schema(description = "主键ID", example = "1")
    private int id;

    @Schema(description = "供应商名称", example = "某某建材有限公司")
    private String supplier_name;

    @Schema(description = "联系人", example = "张三")
    private String contact_person;

    @Schema(description = "联系电话", example = "13800000000")
    private String contact_phone;

    @Schema(description = "供应商地址", example = "广东省深圳市")
    private String address;

    @Schema(description = "备注信息", example = "长期合作")
    private String remark;

    @Schema(description = "合作状态 0停用1正常合作", example = "1")
    private int status;

    @Schema(description = "逻辑删除 0未删1已删", example = "0")
    private int is_deleted;
}
