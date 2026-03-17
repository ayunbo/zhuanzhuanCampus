package com.zhuanzhuan.controller.admin.category;

import com.zhuanzhuan.dto.CategoryPageQueryDTO;
import com.zhuanzhuan.dto.CategorySaveDTO;
import com.zhuanzhuan.dto.CategorySortDTO;
import com.zhuanzhuan.dto.CategoryStatusDTO;
import com.zhuanzhuan.dto.CategoryUpdateDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.service.category.CategoryService;
import com.zhuanzhuan.vo.CategoryTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "管理员分类管理接口")
@RestController
@RequestMapping("/admin/category")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "新增分类（新增时不需要传分类ID）")
    @PostMapping
    public Result<Void> create(@RequestBody CategorySaveDTO categorySaveDTO) {
        categoryService.createCategory(categorySaveDTO);
        return Result.success();
    }

    @Operation(summary = "分页查询分类（支持按名称模糊查询）")
    @GetMapping("/page")
    public Result<PageResult> pageQuery(CategoryPageQueryDTO pageQueryDTO) {
        return Result.success(categoryService.pageQueryCategory(pageQueryDTO));
    }

    @Operation(summary = "查询分类树（管理端）")
    @GetMapping("/tree")
    public Result<List<CategoryTreeVO>> tree() {
        return Result.success(categoryService.listCategoryTree(false));
    }

    @Operation(summary = "修改分类")
    @PutMapping
    public Result<Void> update(@RequestBody CategoryUpdateDTO categoryUpdateDTO) {
        categoryService.updateCategory(categoryUpdateDTO);
        return Result.success();
    }

    @Operation(summary = "修改分类状态（启用/禁用）")
    @PutMapping("/status")
    public Result<Void> updateStatus(@RequestBody CategoryStatusDTO categoryStatusDTO) {
        categoryService.updateCategoryStatus(categoryStatusDTO);
        return Result.success();
    }

    @Operation(summary = "修改分类排序")
    @PutMapping("/sort")
    public Result<Void> updateSort(@RequestBody CategorySortDTO categorySortDTO) {
        categoryService.updateCategorySort(categorySortDTO);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }
}
