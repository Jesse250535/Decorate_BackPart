package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.DesignQuoteItem;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 报价单明细 Mapper
 */
@Mapper
public interface DesignQuoteItemMapper {

    @Insert("insert into design_quote_item (quote_id,item_type,stage_code,item_name,spec,unit,quantity,unit_price," +
            "amount,brand,remark,sort,is_deleted) values(#{quote_id},#{item_type},#{stage_code},#{item_name},#{spec}," +
            "#{unit},#{quantity},#{unit_price},#{amount},#{brand},#{remark},#{sort},#{is_deleted})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DesignQuoteItem item);

    @Results(id = "quoteItemResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "quote_id", column = "quote_id"),
            @Result(property = "item_type", column = "item_type"),
            @Result(property = "stage_code", column = "stage_code"),
            @Result(property = "item_name", column = "item_name"),
            @Result(property = "spec", column = "spec"),
            @Result(property = "unit", column = "unit"),
            @Result(property = "quantity", column = "quantity"),
            @Result(property = "unit_price", column = "unit_price"),
            @Result(property = "amount", column = "amount"),
            @Result(property = "brand", column = "brand"),
            @Result(property = "remark", column = "remark"),
            @Result(property = "sort", column = "sort"),
            @Result(property = "create_time", column = "create_time"),
            @Result(property = "is_deleted", column = "is_deleted")
    })
    @Select("select * from design_quote_item where id = #{id}")
    DesignQuoteItem findById(@Param("id") int id);

    @ResultMap("quoteItemResult")
    @Select("select * from design_quote_item where quote_id = #{quote_id} and is_deleted = 0 order by sort asc, id asc")
    List<DesignQuoteItem> findByQuote(@Param("quote_id") int quoteId);

    /**
     * 重算报价前先逻辑删除旧明细，避免历史数据串场
     */
    @Update("update design_quote_item set is_deleted = 1 where quote_id = #{quote_id}")
    int deleteByQuote(@Param("quote_id") int quoteId);

    /**
     * 按硬装/软装汇总金额，明细为空时返回 0
     */
    @Select("select coalesce(sum(amount),0) from design_quote_item " +
            "where quote_id = #{quote_id} and item_type = #{item_type} and is_deleted = 0")
    BigDecimal sumByType(@Param("quote_id") int quoteId, @Param("item_type") int itemType);
}
