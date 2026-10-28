package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.DecorationCategory;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 分类标签 Mapper
 * <p>
 * 原本只提供下拉选项查询，库管页面需要管理分类后补充了增删改查。
 * 删除走逻辑删除（is_deleted = 1），因为 decoration_material / decoration_plan
 * 对 category_id 都有 ON DELETE RESTRICT 外键，物理删除必然失败。
 */
@Mapper
public interface DecorationCategoryMapper {

    /**
     * 按分类类型查询未删除的分类
     * <p>
     * 显式写 @Results 而不用自动映射：实体属性名与列名均为下划线风格，
     * 一旦全局开启驼峰映射就会失配，显式映射可以避免这个不确定性。
     *
     * @param category_type 1装修方案分类 2装修材料分类
     * @return 分类列表，按排序权重升序
     */
    @Results(id = "categoryResult", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "category_name", column = "category_name"),
            @Result(property = "category_type", column = "category_type"),
            @Result(property = "parent_id", column = "parent_id"),
            @Result(property = "sort", column = "sort"),
            @Result(property = "is_hot", column = "is_hot"),
            @Result(property = "create_time", column = "create_time"),
            @Result(property = "is_deleted", column = "is_deleted")
    })
    @Select("select * from decoration_category where category_type = #{category_type} " +
            "and is_deleted = 0 order by sort asc, id asc")
    List<DecorationCategory> findByType(@Param("category_type") int category_type);

    /**
     * 全部分类（含两种类型、含已逻辑删除），供管理页筛选展示
     */
    @ResultMap("categoryResult")
    @Select("select * from decoration_category order by category_type asc, sort asc, id asc")
    List<DecorationCategory> findAll();

    @ResultMap("categoryResult")
    @Select("select * from decoration_category where id = #{id}")
    DecorationCategory findById(@Param("id") int id);

    /**
     * 新增分类。create_time / is_deleted 交给数据库默认值，避免时间与时区问题
     */
    @Insert("insert into decoration_category(category_name, category_type, parent_id, `sort`, is_hot) " +
            "values(#{category_name}, #{category_type}, #{parent_id}, #{sort}, #{is_hot})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(DecorationCategory category);

    @Update("update decoration_category set category_name = #{category_name}, " +
            "category_type = #{category_type}, parent_id = #{parent_id}, " +
            "`sort` = #{sort}, is_hot = #{is_hot} where id = #{id}")
    int update(DecorationCategory category);

    @Update("update decoration_category set is_deleted = 1 where id = #{id}")
    int softDelete(@Param("id") int id);
}
