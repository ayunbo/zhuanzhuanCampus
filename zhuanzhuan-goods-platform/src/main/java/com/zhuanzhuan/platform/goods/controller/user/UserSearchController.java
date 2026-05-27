package com.zhuanzhuan.platform.goods.controller.user;

import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.service.SearchAssistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户搜索辅助接口")
@RestController
@RequestMapping("/user/search")
public class UserSearchController {

    @Autowired
    private SearchAssistService searchAssistService;

    @Operation(summary = "获取联想词")
    @GetMapping("/suggest")
    public Result<List<String>> suggest(@RequestParam String keyword,
                                        @RequestParam(required = false) Integer limit) {
        return Result.success(searchAssistService.suggest(keyword, limit));
    }

    @Operation(summary = "获取热门搜索词")
    @GetMapping("/hot")
    public Result<List<String>> hot(@RequestParam(required = false) Integer limit) {
        return Result.success(searchAssistService.hot(limit));
    }
}
