package com.zhuanzhuan.platform.collect.controller.user;

import com.zhuanzhuan.dto.FavoritePageQueryDTO;
import com.zhuanzhuan.platform.collect.service.FavoriteService;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.vo.FavoriteStatusVO;
import com.zhuanzhuan.vo.FavoriteToggleVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    @PostMapping("/{goodsId:\\d+}")
    public Result<FavoriteToggleVO> collect(@PathVariable Long goodsId) {
        return Result.success(favoriteService.collect(goodsId));
    }

    @DeleteMapping("/{goodsId:\\d+}")
    public Result<FavoriteToggleVO> cancel(@PathVariable Long goodsId) {
        return Result.success(favoriteService.cancel(goodsId));
    }

    @GetMapping("/{goodsId:\\d+}/status")
    public Result<FavoriteStatusVO> status(@PathVariable Long goodsId) {
        return Result.success(favoriteService.status(goodsId));
    }

    @GetMapping({"", "/page"})
    public Result<PageResult> page(FavoritePageQueryDTO dto) {
        return Result.success(favoriteService.page(dto));
    }
}
