package com.zhuanzhuan.platform.goods.mapper;

import com.zhuanzhuan.annotation.AutoFill;
import com.zhuanzhuan.dto.CategoryPageQueryDTO;
import com.zhuanzhuan.entity.Category;
import com.zhuanzhuan.enumeration.OperationType;
import com.zhuanzhuan.vo.CategoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {

    Category getById(Long id);

    int countByParentIdAndName(@Param("parentId") Long parentId, @Param("name") String name);

    int countByParentIdAndNameExcludeId(@Param("parentId") Long parentId,
                                        @Param("name") String name,
                                        @Param("id") Long id);

    @AutoFill(OperationType.INSERT)
    int insert(Category category);

    @AutoFill(OperationType.UPDATE)
    int updateByIdSelective(Category category);

    List<CategoryVO> pageQuery(CategoryPageQueryDTO pageQueryDTO);

    List<Category> listByStatus(@Param("status") Integer status);

    List<Category> listByParentId(@Param("parentId") Long parentId);

    int countChildrenByParentId(@Param("parentId") Long parentId);

    int countGoodsByCategoryId(@Param("categoryId") Long categoryId);

    int deleteById(@Param("id") Long id);
}
