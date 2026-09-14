package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.SysHomeConfig;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 首页配置 Mapper
 */
@Mapper
public interface SysHomeConfigMapper {

    @Insert("insert into sys_home_config (config_type,image_url,jump_link,sort,status,is_deleted) " +
            "values(#{config_type},#{image_url},#{jump_link},#{sort},#{status},#{is_deleted})")
    int insert(SysHomeConfig config);

    @Results(id = "homeConfigResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "config_type", column = "config_type"),
            @Result(property = "image_url", column = "image_url"),
            @Result(property = "jump_link", column = "jump_link"),
            @Result(property = "sort", column = "sort"),
            @Result(property = "status", column = "status"),
            @Result(property = "is_deleted", column = "is_deleted")
    })
    @Select("select * from sys_home_config where id = #{id}")
    SysHomeConfig findById(@Param("id") int id);

    @ResultMap("homeConfigResult")
    @Select("select * from sys_home_config")
    List<SysHomeConfig> findAll();

    @Update("update sys_home_config set config_type=#{config_type},image_url=#{image_url}," +
            "jump_link=#{jump_link},sort=#{sort},status=#{status},is_deleted=#{is_deleted} where id=#{id}")
    int update(SysHomeConfig config);

    @Delete("delete from sys_home_config where id = #{id}")
    int delete(@Param("id") int id);
}
