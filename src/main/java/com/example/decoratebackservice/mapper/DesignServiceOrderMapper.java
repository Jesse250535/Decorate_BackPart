package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.DesignServiceOrder;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 设计服务单 Mapper
 * <p>
 * 状态流转统一使用「条件 UPDATE」：WHERE 里带上期望的旧状态，
 * 返回 0 行说明状态已被别人改动（例如业主重复点确认），由 Service 转成友好提示，
 * 不依赖先查后改，天然避免并发覆盖。
 */
@Mapper
public interface DesignServiceOrderMapper {

    @Insert("insert into design_service_order (service_no,user_id,designer_id,manager_id,plan_id,decoration_mode," +
            "house_type,house_area,address,style,budget,expect_date,requirement,status,deposit_amount,is_deleted) " +
            "values(#{service_no},#{user_id},#{designer_id},#{manager_id},#{plan_id},#{decoration_mode}," +
            "#{house_type},#{house_area},#{address},#{style},#{budget},#{expect_date},#{requirement},#{status}," +
            "#{deposit_amount},#{is_deleted})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DesignServiceOrder order);

    @Results(id = "dsoResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "service_no", column = "service_no"),
            @Result(property = "user_id", column = "user_id"),
            @Result(property = "designer_id", column = "designer_id"),
            @Result(property = "manager_id", column = "manager_id"),
            @Result(property = "plan_id", column = "plan_id"),
            @Result(property = "decoration_mode", column = "decoration_mode"),
            @Result(property = "house_type", column = "house_type"),
            @Result(property = "house_area", column = "house_area"),
            @Result(property = "address", column = "address"),
            @Result(property = "style", column = "style"),
            @Result(property = "budget", column = "budget"),
            @Result(property = "expect_date", column = "expect_date"),
            @Result(property = "requirement", column = "requirement"),
            @Result(property = "status", column = "status"),
            @Result(property = "measure_date", column = "measure_date"),
            @Result(property = "plan_drawing_time", column = "plan_drawing_time"),
            @Result(property = "plan_drawings", column = "plan_drawings"),
            @Result(property = "plan_confirm_time", column = "plan_confirm_time"),
            @Result(property = "deposit_amount", column = "deposit_amount"),
            @Result(property = "deposit_time", column = "deposit_time"),
            @Result(property = "effect_drawing_time", column = "effect_drawing_time"),
            @Result(property = "effect_drawings", column = "effect_drawings"),
            @Result(property = "quote_id", column = "quote_id"),
            @Result(property = "quote_time", column = "quote_time"),
            @Result(property = "contract_id", column = "contract_id"),
            @Result(property = "contract_time", column = "contract_time"),
            @Result(property = "start_date", column = "start_date"),
            @Result(property = "finish_time", column = "finish_time"),
            @Result(property = "cancel_reason", column = "cancel_reason"),
            @Result(property = "create_time", column = "create_time"),
            @Result(property = "update_time", column = "update_time"),
            @Result(property = "is_deleted", column = "is_deleted")
    })
    @Select("select * from design_service_order where id = #{id}")
    DesignServiceOrder findById(@Param("id") int id);

    @ResultMap("dsoResult")
    @Select("select * from design_service_order where is_deleted = 0 order by id desc")
    List<DesignServiceOrder> findAll();

    @ResultMap("dsoResult")
    @Select("select * from design_service_order where user_id = #{user_id} and is_deleted = 0 order by id desc")
    List<DesignServiceOrder> findByUser(@Param("user_id") int user_id);

    @ResultMap("dsoResult")
    @Select("select * from design_service_order where designer_id = #{designer_id} and is_deleted = 0 order by id desc")
    List<DesignServiceOrder> findByDesigner(@Param("designer_id") int designer_id);

    @ResultMap("dsoResult")
    @Select("select * from design_service_order where manager_id = #{manager_id} and is_deleted = 0 order by id desc")
    List<DesignServiceOrder> findByManager(@Param("manager_id") int manager_id);

    @ResultMap("dsoResult")
    @Select("select * from design_service_order where manager_id is null and status = 8 and is_deleted = 0 order by id desc")
    List<DesignServiceOrder> findWaitContract();

    /**
     * 项目经理：所有已签合同的装修服务单（业主在线签约后即可看到施工项目）
     */
    @ResultMap("dsoResult")
    @Select("select * from design_service_order where status >= 9 and is_deleted = 0 order by id desc")
    List<DesignServiceOrder> findSignedProjects();

    /**
     * 通用状态推进：只有当前状态仍等于 expect 时才生效
     */
    @Update("update design_service_order set status = #{next} where id = #{id} and status = #{expect} and is_deleted = 0")
    int advance(@Param("id") int id, @Param("expect") int expect, @Param("next") int next);

    @Update("update design_service_order set status = 2, measure_date = #{time} where id = #{id} and status = 1 and is_deleted = 0")
    int markMeasure(@Param("id") int id, @Param("time") LocalDateTime time);

    @Update("update design_service_order set status = 3, plan_drawing_time = #{time}, plan_drawings = #{drawings}, " +
            "deposit_amount = #{deposit_amount} where id = #{id} and status = 2 and is_deleted = 0")
    int markPlanDrawing(@Param("id") int id, @Param("time") LocalDateTime time,
                        @Param("drawings") String drawings,
                        @Param("deposit_amount") java.math.BigDecimal depositAmount);

    @Update("update design_service_order set status = 4, plan_confirm_time = #{time} where id = #{id} and status = 3 and is_deleted = 0")
    int markPlanConfirm(@Param("id") int id, @Param("time") LocalDateTime time);

    @Update("update design_service_order set status = 5, deposit_amount = #{amount}, deposit_time = #{time} " +
            "where id = #{id} and status = 4 and is_deleted = 0")
    int markDeposit(@Param("id") int id, @Param("amount") java.math.BigDecimal amount, @Param("time") LocalDateTime time);

    @Update("update design_service_order set status = 6, effect_drawing_time = #{time}, effect_drawings = #{drawings} " +
            "where id = #{id} and status = 5 and is_deleted = 0")
    int markEffectDrawing(@Param("id") int id, @Param("time") LocalDateTime time, @Param("drawings") String drawings);

    @Update("update design_service_order set status = 7, quote_id = #{quote_id}, quote_time = #{time} " +
            "where id = #{id} and status = 6 and is_deleted = 0")
    int markQuote(@Param("id") int id, @Param("quote_id") int quote_id, @Param("time") LocalDateTime time);

    @Update("update design_service_order set status = 9, contract_id = #{contract_id}, manager_id = #{manager_id}, " +
            "contract_time = #{time}, start_date = #{start_date} where id = #{id} and status = 8 and is_deleted = 0")
    int markContract(@Param("id") int id, @Param("contract_id") int contract_id,
                     @Param("manager_id") int manager_id, @Param("time") LocalDateTime time,
                     @Param("start_date") LocalDate start_date);

    @Update("update design_service_order set status = 10 where id = #{id} and status = 9 and is_deleted = 0")
    int markStart(@Param("id") int id);

    /**
     * 业主线上签合同：平面图确认后（4~8）任一状态 → 已签合同（9）
     */
    @Update("update design_service_order set status = 9, contract_id = #{contract_id}, " +
            "manager_id = ifnull(#{manager_id,jdbcType=BIGINT}, manager_id), contract_time = #{time}, " +
            "start_date = #{start_date} where id = #{id} and status between 4 and 8 and is_deleted = 0")
    int markOwnerContract(@Param("id") int id, @Param("contract_id") int contract_id,
                          @Param("manager_id") Integer manager_id, @Param("time") LocalDateTime time,
                          @Param("start_date") LocalDate start_date);

    @Update("update design_service_order set status = 11 where id = #{id} and status = 10 and is_deleted = 0")
    int markWaitFinal(@Param("id") int id);

    @Update("update design_service_order set status = 12, finish_time = #{time} where id = #{id} and status = 11 and is_deleted = 0")
    int markFinish(@Param("id") int id, @Param("time") LocalDateTime time);

    @Update("update design_service_order set status = 13, cancel_reason = #{reason} " +
            "where id = #{id} and status < 9 and is_deleted = 0")
    int markCancel(@Param("id") int id, @Param("reason") String reason);

    @Update("update design_service_order set manager_id = #{manager_id} where id = #{id} and is_deleted = 0")
    int bindManager(@Param("id") int id, @Param("manager_id") int manager_id);
}
