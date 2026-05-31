package com.zhuanzhuan.platform.history.controller.user;

import com.zhuanzhuan.dto.SearchHistoryQueryDTO;
import com.zhuanzhuan.dto.SearchHistoryRecordDTO;
import com.zhuanzhuan.platform.history.service.SearchHistoryService;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.vo.SearchHistoryArchiveVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "用户搜索历史接口")
@RestController
@RequestMapping("/user/search/history")
public class SearchHistoryController {

    @Autowired
    private SearchHistoryService searchHistoryService;

    @Operation(summary = "记录一次搜索历史")
    @PostMapping
    public Result<Void> record(@RequestBody SearchHistoryRecordDTO dto) {
        searchHistoryService.record(dto);
        return Result.success();
    }

    @Operation(summary = "查询搜索历史")
    @GetMapping({"", "/archive"})
    public Result<List<SearchHistoryArchiveVO>> archive(SearchHistoryQueryDTO dto) {
        return Result.success(searchHistoryService.archive(dto));
    }

    @Operation(summary = "删除单条搜索历史")
    @DeleteMapping("/{historyId:\\d+}")
    public Result<Void> remove(@PathVariable Long historyId) {
        searchHistoryService.remove(historyId);
        return Result.success();
    }

    @Operation(summary = "清空搜索历史")
    @DeleteMapping("/clear")
    public Result<Void> clear() {
        searchHistoryService.clear();
        return Result.success();
    }
}
