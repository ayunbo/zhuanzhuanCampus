package com.zhuanzhuan.service.category;

import com.zhuanzhuan.dto.CategoryPageQueryDTO;
import com.zhuanzhuan.dto.CategorySaveDTO;
import com.zhuanzhuan.dto.CategorySortDTO;
import com.zhuanzhuan.dto.CategoryStatusDTO;
import com.zhuanzhuan.dto.CategoryUpdateDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.CategoryTreeVO;

import java.util.List;

public interface CategoryService {

    void createCategory(CategorySaveDTO categorySaveDTO);

    PageResult pageQueryCategory(CategoryPageQueryDTO pageQueryDTO);

    List<CategoryTreeVO> listCategoryTree(boolean onlyEnable);

    void updateCategory(CategoryUpdateDTO categoryUpdateDTO);

    void updateCategoryStatus(CategoryStatusDTO categoryStatusDTO);

    void updateCategorySort(CategorySortDTO categorySortDTO);

    void deleteCategory(Long id);
}
