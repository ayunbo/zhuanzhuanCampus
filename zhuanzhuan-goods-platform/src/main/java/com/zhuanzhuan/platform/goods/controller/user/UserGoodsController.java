package com.zhuanzhuan.platform.goods.controller.user;

import com.zhuanzhuan.dto.GoodsPageQueryDTO;
import com.zhuanzhuan.dto.GoodsStatUpdateDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.platform.goods.service.GoodsService;
import com.zhuanzhuan.vo.GoodsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/goods")
public class UserGoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("/page")
    public Result<PageResult> page(GoodsPageQueryDTO dto) {
        return Result.success(goodsService.pageForUser(dto));
    }

    @GetMapping("/{id}")
    public Result<GoodsVO> detail(@PathVariable Long id) {
        return Result.success(goodsService.getDetail(id));
    }

    @PutMapping("/stats")
    public Result<Void> updateStats(@RequestBody GoodsStatUpdateDTO dto) {
        goodsService.updateStats(dto);
        return Result.success();
    }
}
