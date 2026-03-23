package com.zhuanzhuan.platform.goods.controller.user;

import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.platform.goods.service.CategoryService;
import com.zhuanzhuan.vo.CategoryTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户分类查询接口")
@RestController
@RequestMapping("/user/category")
public class UserCategoryController {

    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "查询可用分类树（仅启用）")
    @GetMapping("/tree")
    public Result<List<CategoryTreeVO>> tree() {
        return Result.success(categoryService.listCategoryTree(true));
    }
}
