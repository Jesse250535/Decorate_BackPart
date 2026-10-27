package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.DecorationMaterial;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 装修材料 Mapper
 */
@Mapper
public interface DecorationMaterialMapper {

    @Insert("insert into decoration_material (material_name,cover_image,price,spec_long,spec_width,spec_height," +
            "unit,stock,description,category_id,supplier_id,audit_status,audit_remark,sales_count,status,is_deleted) " +
            "values(#{material_name},#{cover_image},#{price},#{spec_long},#{spec_width},#{spec_height},#{unit}," +
            "#{stock},#{description},#{category_id},#{supplier_id},#{audit_status},#{audit_remark},#{sales_count}," +
            "#{status},#{is_deleted})")
    int insert(DecorationMaterial material);

    @Results(id = "materialResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "material_name", column = "material_name"),
            @Result(property = "cover_image", column = "cover_image"),
            @Result(property = "price", column = "price"),
            @Result(property = "spec_long", column = "spec_long"),
            @Result(property = "spec_width", column = "spec_width"),
            @Result(property = "spec_height", column = "spec_height"),
            @Result(property = "unit", column = "unit"),
            @Result(property = "stock", column = "stock"),
            @Result(property = "description", column = "description"),
            @Result(property = "category_id", column = "category_id"),
            @Result(property = "supplier_id", column = "supplier_id"),
            @Result(property = "audit_status", column = "audit_status"),
            @Result(property = "audit_remark", column = "audit_remark"),
            @Result(property = "sales_count", column = "sales_count"),
            @Result(property = "status", column = "status"),
            @Result(property = "is_deleted", column = "is_deleted")
    })
    @Select("select * from decoration_material where id = #{id}")
    DecorationMaterial findById(@Param("id") int id);

    @ResultMap("materialResult")
    @Select("select * from decoration_material")
    List<DecorationMaterial> findAll();

    @Update("<script>" +
            "update decoration_material " +
            "<set>" +
            "<if test='material_name != null'>material_name=#{material_name},</if>" +
            "<if test='cover_image != null'>cover_image=#{cover_image},</if>" +
            "<if test='price != null'>price=#{price},</if>" +
            "<if test='spec_long != null'>spec_long=#{spec_long},</if>" +
            "<if test='spec_width != null'>spec_width=#{spec_width},</if>" +
            "<if test='spec_height != null'>spec_height=#{spec_height},</if>" +
            "<if test='unit != null'>unit=#{unit},</if>" +
            "<if test='stock != 0'>stock=#{stock},</if>" +
            "<if test='description != null'>description=#{description},</if>" +
            "<if test='category_id != 0'>category_id=#{category_id},</if>" +
            "<if test='supplier_id != 0'>supplier_id=#{supplier_id},</if>" +
            "<if test='audit_status != 0'>audit_status=#{audit_status},</if>" +
            "<if test='audit_remark != null'>audit_remark=#{audit_remark},</if>" +
            "<if test='sales_count != null'>sales_count=#{sales_count},</if>" +
            "<if test='status != 0'>status=#{status},</if>" +
            "<if test='is_deleted != 0'>is_deleted=#{is_deleted},</if>" +
            "</set>" +
            "where id=#{id}" +
            "</script>")
    int update(DecorationMaterial material);

    @Delete("delete from decoration_material where id = #{id}")
    int delete(@Param("id") int id);

    /**
     * 单独更新上架状态。
     * <p>
     * 既有的 update 用 {@code <if test='status != 0'>} 判断，把 0 当成「未提供」，
     * 因此无法通过它下架材料；库管需要下架能力，这里补一条专用语句。
     *
     * @param id     材料ID
     * @param status 0下架 1上架
     * @return 受影响行数
     */
    @Update("update decoration_material set status = #{status} where id = #{id}")
    int updateStatus(@Param("id") int id, @Param("status") int status);

    /**
     * 单独更新库存数量，理由同上：既有 update 无法把库存改为 0
     *
     * @param id    材料ID
     * @param stock 库存数量
     * @return 受影响行数
     */
    @Update("update decoration_material set stock = #{stock} where id = #{id}")
    int updateStock(@Param("id") int id, @Param("stock") int stock);

    /**
     * 逻辑删除材料（把 is_deleted 置 1）。
     * <p>
     * 为什么不用上面的物理 delete：material_stock_record 有外键
     * {@code fk_stock_material ... ON DELETE RESTRICT}，只要该材料有入库/出库流水，
     * 物理删除就会抛 SQLIntegrityConstraintViolationException。
     * 逻辑删除既能保住历史流水，也不影响仍在使用物理删除的其它调用方。
     *
     * @param id 材料ID
     * @return 受影响行数
     */
    @Update("update decoration_material set is_deleted = 1 where id = #{id}")
    int softDelete(@Param("id") int id);

    @Select("select * from decoration_material where material_name = #{material_name}")
    DecorationMaterial findByMaterialName(@Param("material_name") String material_name);
}
