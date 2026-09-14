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

    @Update("update decoration_material set material_name=#{material_name},cover_image=#{cover_image}," +
            "price=#{price},spec_long=#{spec_long},spec_width=#{spec_width},spec_height=#{spec_height},unit=#{unit}," +
            "stock=#{stock},description=#{description},category_id=#{category_id},supplier_id=#{supplier_id}," +
            "audit_status=#{audit_status},audit_remark=#{audit_remark},sales_count=#{sales_count},status=#{status}," +
            "is_deleted=#{is_deleted} where id=#{id}")
    int update(DecorationMaterial material);

    @Delete("delete from decoration_material where id = #{id}")
    int delete(@Param("id") int id);
}
