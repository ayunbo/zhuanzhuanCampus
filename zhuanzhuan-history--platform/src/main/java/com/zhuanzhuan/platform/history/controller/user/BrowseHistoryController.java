package com.zhuanzhuan.platform.history.controller.user;

import com.zhuanzhuan.dto.BrowseHistoryQueryDTO;
import com.zhuanzhuan.platform.history.service.BrowseHistoryService;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.vo.UserBrowseHistoryArchiveVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/history")
public class BrowseHistoryController {

    @Autowired
    private BrowseHistoryService browseHistoryService;

    @PostMapping("/{goodsId:\\d+}")
    public Result<Void> record(@PathVariable Long goodsId) {
        browseHistoryService.record(goodsId);
        return Result.success();
    }

    @GetMapping({"", "/archive"})
    public Result<List<UserBrowseHistoryArchiveVO>> archive(BrowseHistoryQueryDTO dto) {
        return Result.success(browseHistoryService.archive(dto));
    }

    @DeleteMapping("/{historyId:\\d+}")
    public Result<Void> remove(@PathVariable Long historyId) {
        browseHistoryService.remove(historyId);
        return Result.success();
    }

    @DeleteMapping("/clear")
    public Result<Void> clear() {
        browseHistoryService.clear();
        return Result.success();
    }
}
