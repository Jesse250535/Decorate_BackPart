package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.SysNotice;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 系统公告 Mapper
 */
@Mapper
public interface SysNoticeMapper {

    @Insert("insert into sys_notice (title,content,show_position,publish_time,expire_time,status," +
            "create_user_id,is_deleted) " +
            "values(#{title},#{content},#{show_position},#{publish_time},#{expire_time},#{status}," +
            "#{create_user_id},#{is_deleted})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SysNotice notice);

    @Results(id = "noticeResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "show_position", column = "show_position"),
            @Result(property = "publish_time", column = "publish_time"),
            @Result(property = "expire_time", column = "expire_time"),
            @Result(property = "status", column = "status"),
            @Result(property = "create_user_id", column = "create_user_id"),
            @Result(property = "is_deleted", column = "is_deleted")
    })
    @Select("select * from sys_notice where id = #{id} and is_deleted = 0")
    SysNotice findById(@Param("id") Long id);

    @ResultMap("noticeResult")
    @Select("select * from sys_notice where is_deleted = 0 order by publish_time desc")
    List<SysNotice> findAll();

    /**
     * 按发布状态 / 展示位置条件查询
     * 两个条件均可为空，为空时不参与过滤
     */
    @ResultMap("noticeResult")
    @Select("<script>" +
            "select * from sys_notice " +
            "<where>" +
            "is_deleted = 0 " +
            "<if test='status != null'>and status = #{status} </if>" +
            "<if test='show_position != null'>and show_position = #{show_position} </if>" +
            "</where>" +
            "order by publish_time desc" +
            "</script>")
    List<SysNotice> findByCondition(@Param("status") Integer status,
                                    @Param("show_position") Integer show_position);

    /**
     * 前台公开查询：只取「已发布」的公告，可限定展示位置、是否过滤已过期、最多返回条数
     * <p>
     * 三个条件均可为空：show_position 为空则不限位置，limit 为空则不限制条数
     *
     * @param show_position 展示位置 1首页顶部 2用户中心 3后台首页
     * @param active_only   是否只取未过期（expire_time 为 null 视为长期有效）
     * @param limit         最大返回条数
     */
    @ResultMap("noticeResult")
    @Select("<script>" +
            "select * from sys_notice " +
            "where is_deleted = 0 and status = 1 " +
            "<if test='show_position != null'>and show_position = #{show_position} </if>" +
            "<if test='active_only'>and (expire_time is null or expire_time &gt; now()) </if>" +
            "order by publish_time desc, id desc " +
            "<if test='limit != null'>limit #{limit}</if>" +
            "</script>")
    List<SysNotice> findPublished(@Param("show_position") Integer show_position,
                                  @Param("active_only") boolean active_only,
                                  @Param("limit") Integer limit);

    /**
     * 动态更新：仅更新传入（非 null）的字段
     */
    @Update("<script>" +
            "update sys_notice " +
            "<set>" +
            "<if test='title != null'>title=#{title},</if>" +
            "<if test='content != null'>content=#{content},</if>" +
            "<if test='show_position != null'>show_position=#{show_position},</if>" +
            "<if test='publish_time != null'>publish_time=#{publish_time},</if>" +
            "<if test='expire_time != null'>expire_time=#{expire_time},</if>" +
            "<if test='status != null'>status=#{status},</if>" +
            "<if test='create_user_id != null'>create_user_id=#{create_user_id},</if>" +
            "</set>" +
            "where id=#{id} and is_deleted = 0" +
            "</script>")
    int update(SysNotice notice);

    /**
     * 逻辑删除（将 is_deleted 置为 1）
     */
    @Update("update sys_notice set is_deleted = 1 where id = #{id}")
    int deleteById(@Param("id") Long id);
}
