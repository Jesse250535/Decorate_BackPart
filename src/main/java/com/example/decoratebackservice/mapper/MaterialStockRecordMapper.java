package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.MaterialStockRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 材料出入库记录 Mapper
 */
@Mapper
public interface MaterialStockRecordMapper {

    @Insert("insert into material_stock_record (material_id,operate_type,change_num,before_stock,after_stock," +
            "operate_user_id,remark,is_deleted) values(#{material_id},#{operate_type},#{change_num},#{before_stock}," +
            "#{after_stock},#{operate_user_id},#{remark},#{is_deleted})")
    int insert(MaterialStockRecord record);

    @Results(id = "stockRecordResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "material_id", column = "material_id"),
            @Result(property = "operate_type", column = "operate_type"),
            @Result(property = "change_num", column = "change_num"),
            @Result(property = "before_stock", column = "before_stock"),
            @Result(property = "after_stock", column = "after_stock"),
            @Result(property = "operate_user_id", column = "operate_user_id"),
            @Result(property = "remark", column = "remark"),
            @Result(property = "is_deleted", column = "is_deleted")
    })
    @Select("select * from material_stock_record where id = #{id}")
    MaterialStockRecord findById(@Param("id") int id);

    @ResultMap("stockRecordResult")
    @Select("select * from material_stock_record")
    List<MaterialStockRecord> findAll();

    @Update("update material_stock_record set material_id=#{material_id},operate_type=#{operate_type}," +
            "change_num=#{change_num},before_stock=#{before_stock},after_stock=#{after_stock}," +
            "operate_user_id=#{operate_user_id},remark=#{remark},is_deleted=#{is_deleted} where id=#{id}")
    int update(MaterialStockRecord record);

    @Delete("delete from material_stock_record where id = #{id}")
    int delete(@Param("id") int id);
}
