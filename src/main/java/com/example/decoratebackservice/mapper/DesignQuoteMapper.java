package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.DesignQuote;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 报价单 Mapper
 */
@Mapper
public interface DesignQuoteMapper {

    @Insert("insert into design_quote (quote_no,service_id,user_id,designer_id,hard_amount,soft_amount,total_amount," +
            "status,remark,is_deleted) values(#{quote_no},#{service_id},#{user_id},#{designer_id},#{hard_amount}," +
            "#{soft_amount},#{total_amount},#{status},#{remark},#{is_deleted})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DesignQuote quote);

    @Results(id = "quoteResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "quote_no", column = "quote_no"),
            @Result(property = "service_id", column = "service_id"),
            @Result(property = "user_id", column = "user_id"),
            @Result(property = "designer_id", column = "designer_id"),
            @Result(property = "hard_amount", column = "hard_amount"),
            @Result(property = "soft_amount", column = "soft_amount"),
            @Result(property = "total_amount", column = "total_amount"),
            @Result(property = "status", column = "status"),
            @Result(property = "remark", column = "remark"),
            @Result(property = "confirm_time", column = "confirm_time"),
            @Result(property = "create_time", column = "create_time"),
            @Result(property = "update_time", column = "update_time"),
            @Result(property = "is_deleted", column = "is_deleted")
    })
    @Select("select * from design_quote where id = #{id}")
    DesignQuote findById(@Param("id") int id);

    @ResultMap("quoteResult")
    @Select("select * from design_quote where service_id = #{service_id} and is_deleted = 0 order by id desc")
    List<DesignQuote> findByService(@Param("service_id") int service_id);

    @ResultMap("quoteResult")
    @Select("select * from design_quote where service_id = #{service_id} and status in (1,2) and is_deleted = 0 " +
            "order by id desc limit 1")
    DesignQuote findActiveByService(@Param("service_id") int service_id);

    @Update("update design_quote set status = #{status}, confirm_time = #{confirm_time} " +
            "where id = #{id} and is_deleted = 0")
    int updateStatus(@Param("id") int id, @Param("status") int status,
                     @Param("confirm_time") LocalDateTime confirmTime);

    @Update("update design_quote set hard_amount = #{hard_amount}, soft_amount = #{soft_amount}, " +
            "total_amount = #{total_amount}, remark = #{remark}, status = #{status} where id = #{id} and is_deleted = 0")
    int updateAmounts(@Param("id") int id, @Param("hard_amount") BigDecimal hardAmount,
                      @Param("soft_amount") BigDecimal softAmount, @Param("total_amount") BigDecimal totalAmount,
                      @Param("remark") String remark, @Param("status") int status);

    /**
     * 把同一服务单下其它报价单作废，保证只有一份生效报价
     */
    @Update("update design_quote set status = 3 where service_id = #{service_id} and id <> #{keep_id} " +
            "and status in (0,1) and is_deleted = 0")
    int invalidateOthers(@Param("service_id") int serviceId, @Param("keep_id") int keepId);
}
