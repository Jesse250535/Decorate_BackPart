package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.DesignContract;
import org.apache.ibatis.annotations.*;

/**
 * 装修合同 Mapper（设计服务流程专用）
 */
@Mapper
public interface DesignContractMapper {

    @Insert("insert into design_contract (contract_no,service_id,user_id,designer_id,manager_id,contract_amount," +
            "decoration_mode,sign_status,sign_date,start_date,file_url,remark,is_deleted) values(#{contract_no}," +
            "#{service_id},#{user_id},#{designer_id},#{manager_id},#{contract_amount},#{decoration_mode}," +
            "#{sign_status},#{sign_date},#{start_date},#{file_url},#{remark},#{is_deleted})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DesignContract contract);

    @Results(id = "contractResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "contract_no", column = "contract_no"),
            @Result(property = "service_id", column = "service_id"),
            @Result(property = "user_id", column = "user_id"),
            @Result(property = "designer_id", column = "designer_id"),
            @Result(property = "manager_id", column = "manager_id"),
            @Result(property = "contract_amount", column = "contract_amount"),
            @Result(property = "decoration_mode", column = "decoration_mode"),
            @Result(property = "sign_status", column = "sign_status"),
            @Result(property = "sign_date", column = "sign_date"),
            @Result(property = "start_date", column = "start_date"),
            @Result(property = "file_url", column = "file_url"),
            @Result(property = "remark", column = "remark"),
            @Result(property = "create_time", column = "create_time"),
            @Result(property = "update_time", column = "update_time"),
            @Result(property = "is_deleted", column = "is_deleted")
    })
    @Select("select * from design_contract where id = #{id}")
    DesignContract findById(@Param("id") int id);

    @ResultMap("contractResult")
    @Select("select * from design_contract where service_id = #{service_id} and is_deleted = 0 limit 1")
    DesignContract findByService(@Param("service_id") int serviceId);
}
