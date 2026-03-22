package com.zhuanzhuan.platform.goods.service;

import com.zhuanzhuan.dto.CategoryPageQueryDTO;
import com.zhuanzhuan.dto.CategorySaveDTO;
import com.zhuanzhuan.dto.CategorySortDTO;
import com.zhuanzhuan.dto.CategoryStatusDTO;
import com.zhuanzhuan.dto.CategoryUpdateDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.CategoryTreeVO;

import java.util.List;

/**
 * 分类服务接口。
 */
public interface CategoryService {

    /**
     * 新增分类。
     */
    void createCategory(CategorySaveDTO categorySaveDTO);

    /**
     * 分页查询分类。
     */
    PageResult pageQueryCategory(CategoryPageQueryDTO pageQueryDTO);

    /**
     * 查询分类树。
     */
    List<CategoryTreeVO> listCategoryTree(boolean onlyEnable);

    /**
     * 修改分类。
     */
    void updateCategory(CategoryUpdateDTO categoryUpdateDTO);

    /**
     * 修改分类状态。
     */
    void updateCategoryStatus(CategoryStatusDTO categoryStatusDTO);

    /**
     * 修改分类排序。
     */
    void updateCategorySort(CategorySortDTO categorySortDTO);

    /**
     * 删除分类。
     */
    void deleteCategory(Long id);
}
