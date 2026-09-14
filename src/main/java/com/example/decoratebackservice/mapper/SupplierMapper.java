package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.Supplier;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 材料供应商 Mapper
 */
@Mapper
public interface SupplierMapper {

    @Insert("insert into supplier (supplier_name,contact_person,contact_phone,address,remark,status,is_deleted)" +
            " values(#{supplier_name},#{contact_person},#{contact_phone},#{address},#{remark},#{status},#{is_deleted})")
    int insert(Supplier supplier);

    @Results(id = "supplierResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "supplier_name", column = "supplier_name"),
            @Result(property = "contact_person", column = "contact_person"),
            @Result(property = "contact_phone", column = "contact_phone"),
            @Result(property = "address", column = "address"),
            @Result(property = "remark", column = "remark"),
            @Result(property = "status", column = "status"),
            @Result(property = "is_deleted", column = "is_deleted")
    })
    @Select("select * from supplier where id = #{id}")
    Supplier findById(@Param("id") int id);

    @ResultMap("supplierResult")
    @Select("select * from supplier")
    List<Supplier> findAll();

    @Update("update supplier set supplier_name=#{supplier_name},contact_person=#{contact_person}," +
            "contact_phone=#{contact_phone},address=#{address},remark=#{remark},status=#{status}," +
            "is_deleted=#{is_deleted} where id=#{id}")
    int update(Supplier supplier);

    @Delete("delete from supplier where id = #{id}")
    int delete(@Param("id") int id);
}
