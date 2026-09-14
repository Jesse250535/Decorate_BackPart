package com.example.decoratebackservice.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 材料出入库记录实体（material_stock_record）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "材料出入库记录实体")
public class MaterialStockRecord {

    @Schema(description = "主键ID", example = "1")
    private int id;

    @Schema(description = "关联材料ID", example = "1")
    private int material_id;

    @Schema(description = "操作类型 1采购入库2订单出库3库存盘点调整", example = "1")
    private int operate_type;

    @Schema(description = "变动数量 正数为入库负数为出库", example = "10")
    private int change_num;

    @Schema(description = "变动前库存", example = "100")
    private int before_stock;

    @Schema(description = "变动后库存", example = "110")
    private int after_stock;

    @Schema(description = "操作库管ID", example = "1")
    private int operate_user_id;

    @Schema(description = "出入库备注", example = "采购入库")
    private String remark;

    @Schema(description = "逻辑删除 0未删1已删", example = "0")
    private int is_deleted;
}
