package com.example.decoratebackservice.mapper;

import com.example.decoratebackservice.entity.DecorationCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 分类标签 Mapper（新增，仅供下拉选项使用）
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
}
