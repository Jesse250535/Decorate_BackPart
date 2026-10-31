package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.DesignContract;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;

/**
 * 装修合同 Mapper（设计服务流程专用）
 */
@Mapper
public interface DesignContractMapper {

    @Insert("insert into design_contract (contract_no,service_id,user_id,designer_id,manager_id,contract_amount," +
            "decoration_mode,sign_status,sign_date,start_date,file_url,signature_url,first_payment_amount," +
            "first_payment_time,receipt_url,remark,is_deleted) values(#{contract_no}," +
            "#{service_id},#{user_id},#{designer_id},#{manager_id},#{contract_amount},#{decoration_mode}," +
            "#{sign_status},#{sign_date},#{start_date},#{file_url},#{signature_url},#{first_payment_amount}," +
            "#{first_payment_time},#{receipt_url},#{remark},#{is_deleted})")
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
            @Result(property = "signature_url", column = "signature_url"),
            @Result(property = "first_payment_amount", column = "first_payment_amount"),
            @Result(property = "first_payment_time", column = "first_payment_time"),
            @Result(property = "receipt_url", column = "receipt_url"),
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

    /**
     * 业主线上签署：写入签字图片、合同金额与首付款，并置为已签
     */
    @Update("update design_contract set contract_amount = #{amount}, sign_status = 1, sign_date = #{time}, " +
            "start_date = #{start_date}, signature_url = #{signature_url}, file_url = #{file_url}, " +
            "first_payment_amount = #{first_amount}, remark = #{remark} " +
            "where id = #{id} and sign_status = 0 and is_deleted = 0")
    int updateOwnerSign(@Param("id") int id,
                        @Param("amount") java.math.BigDecimal amount,
                        @Param("first_amount") java.math.BigDecimal firstAmount,
                        @Param("signature_url") String signatureUrl,
                        @Param("file_url") String fileUrl,
                        @Param("start_date") java.time.LocalDate startDate,
                        @Param("remark") String remark,
                        @Param("time") LocalDateTime time);

    /**
     * 业主支付首付款：只有未支付过才生效
     */
    @Update("update design_contract set first_payment_time = #{time} " +
            "where id = #{id} and first_payment_time is null and is_deleted = 0")
    int updateFirstPayment(@Param("id") int id, @Param("time") LocalDateTime time);

    /**
     * 业主上传合同回执（纸质合同拍照 / 扫描件）
     */
    @Update("update design_contract set receipt_url = #{receipt_url} where id = #{id} and is_deleted = 0")
    int updateReceipt(@Param("id") int id, @Param("receipt_url") String receiptUrl);
}
