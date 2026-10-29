package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.ConstructionStage;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 施工阶段 Mapper
 * <p>
 * 阶段推进同样用条件 UPDATE 卡旧状态：开工只能从「未开始」进入，
 * 完工只能从「进行中」进入，验收只能在「已完工 / 返工完成」时提交。
 */
@Mapper
public interface ConstructionStageMapper {

    @Insert("insert into construction_stage (service_id,stage_code,stage_name,stage_type,seq,plan_start_date," +
            "plan_end_date,plan_days,status,accept_status,rework_status,manager_id,remark,is_deleted) " +
            "values(#{service_id},#{stage_code},#{stage_name},#{stage_type},#{seq},#{plan_start_date}," +
            "#{plan_end_date},#{plan_days},#{status},#{accept_status},#{rework_status},#{manager_id},#{remark},#{is_deleted})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ConstructionStage stage);

    @Results(id = "stageResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "service_id", column = "service_id"),
            @Result(property = "stage_code", column = "stage_code"),
            @Result(property = "stage_name", column = "stage_name"),
            @Result(property = "stage_type", column = "stage_type"),
            @Result(property = "seq", column = "seq"),
            @Result(property = "plan_start_date", column = "plan_start_date"),
            @Result(property = "plan_end_date", column = "plan_end_date"),
            @Result(property = "actual_start_date", column = "actual_start_date"),
            @Result(property = "actual_end_date", column = "actual_end_date"),
            @Result(property = "plan_days", column = "plan_days"),
            @Result(property = "actual_days", column = "actual_days"),
            @Result(property = "delay_days", column = "delay_days"),
            @Result(property = "on_time", column = "on_time"),
            @Result(property = "status", column = "status"),
            @Result(property = "accept_status", column = "accept_status"),
            @Result(property = "accept_user_id", column = "accept_user_id"),
            @Result(property = "accept_time", column = "accept_time"),
            @Result(property = "accept_remark", column = "accept_remark"),
            @Result(property = "rework_status", column = "rework_status"),
            @Result(property = "rework_days", column = "rework_days"),
            @Result(property = "rework_start_date", column = "rework_start_date"),
            @Result(property = "rework_end_date", column = "rework_end_date"),
            @Result(property = "rework_count", column = "rework_count"),
            @Result(property = "manager_id", column = "manager_id"),
            @Result(property = "images", column = "images"),
            @Result(property = "remark", column = "remark"),
            @Result(property = "create_time", column = "create_time"),
            @Result(property = "update_time", column = "update_time"),
            @Result(property = "is_deleted", column = "is_deleted")
    })
    @Select("select * from construction_stage where id = #{id}")
    ConstructionStage findById(@Param("id") int id);

    @ResultMap("stageResult")
    @Select("select * from construction_stage where service_id = #{service_id} and is_deleted = 0 order by seq asc")
    List<ConstructionStage> findByService(@Param("service_id") int serviceId);

    @ResultMap("stageResult")
    @Select("select * from construction_stage where service_id = #{service_id} and stage_code = #{stage_code} " +
            "and is_deleted = 0 limit 1")
    ConstructionStage findByServiceAndCode(@Param("service_id") int serviceId,
                                           @Param("stage_code") String stageCode);

    @Select("select count(*) from construction_stage where service_id = #{service_id} and is_deleted = 0")
    int countByService(@Param("service_id") int serviceId);

    @Select("select count(*) from construction_stage where service_id = #{service_id} and status = #{status} " +
            "and is_deleted = 0")
    int countByServiceAndStatus(@Param("service_id") int serviceId, @Param("status") int status);

    /**
     * 开工：未开始 → 进行中
     */
    @Update("update construction_stage set status = 1, actual_start_date = #{date} " +
            "where id = #{id} and status = 0 and is_deleted = 0")
    int start(@Param("id") int id, @Param("date") LocalDate date);

    /**
     * 完工并回填工期与是否按时（on_time: 1按时 2超期 0未判定）
     */
    @Update("update construction_stage set status = 2, actual_end_date = #{end_date}, actual_days = #{actual_days}, " +
            "delay_days = #{delay_days}, on_time = #{on_time}, images = #{images}, remark = #{remark} " +
            "where id = #{id} and status in (1,4) and is_deleted = 0")
    int finish(@Param("id") int id, @Param("end_date") LocalDate endDate,
               @Param("actual_days") int actualDays, @Param("delay_days") int delayDays,
               @Param("on_time") int onTime, @Param("images") String images, @Param("remark") String remark);

    /**
     * 业主验收通过：待验收 → 已验收
     */
    @Update("update construction_stage set status = 3, accept_status = 1, accept_user_id = #{user_id}, " +
            "accept_time = #{time}, accept_remark = #{remark} where id = #{id} and status = 2 and is_deleted = 0")
    int acceptPass(@Param("id") int id, @Param("user_id") int userId,
                   @Param("time") LocalDateTime time, @Param("remark") String remark);

    /**
     * 业主验收不通过：进入返工，累计返工次数并登记返工工期
     */
    @Update("update construction_stage set status = 4, accept_status = 2, accept_user_id = #{user_id}, " +
            "accept_time = #{time}, accept_remark = #{remark}, rework_status = 1, rework_days = #{rework_days}, " +
            "rework_start_date = #{rework_start_date}, rework_count = rework_count + 1 " +
            "where id = #{id} and status = 2 and is_deleted = 0")
    int acceptReject(@Param("id") int id, @Param("user_id") int userId, @Param("time") LocalDateTime time,
                     @Param("remark") String remark, @Param("rework_days") int reworkDays,
                     @Param("rework_start_date") LocalDate reworkStartDate);

    /**
     * 返工完成：重新提交验收（返工中 → 已完工待复验）
     */
    @Update("update construction_stage set status = 2, rework_status = 2, rework_end_date = #{end_date}, " +
            "accept_status = 0 where id = #{id} and status = 4 and is_deleted = 0")
    int reworkDone(@Param("id") int id, @Param("end_date") LocalDate endDate);

    /**
     * 项目经理调整计划起止日期
     */
    @Update("update construction_stage set plan_start_date = #{plan_start_date}, plan_end_date = #{plan_end_date}, " +
            "plan_days = #{plan_days} where id = #{id} and status in (0,1) and is_deleted = 0")
    int updatePlan(@Param("id") int id, @Param("plan_start_date") LocalDate planStartDate,
                   @Param("plan_end_date") LocalDate planEndDate, @Param("plan_days") int planDays);
}
