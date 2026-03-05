package com.zhuanzhuan.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TestMapper {

    // 根据 ID 查询员工信息
    @Select("SELECT * FROM employee WHERE id = #{id}")
    Object getById(Long id);

}
