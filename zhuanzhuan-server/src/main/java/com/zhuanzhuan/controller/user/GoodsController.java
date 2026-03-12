package com.zhuanzhuan.controller.user;

import com.zhuanzhuan.entity.Goods;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("/{id}")
    public Result<Goods> getById(@PathVariable Long id) {
        return Result.success(goodsService.getById(id));
    }


    @GetMapping("/list")
    public Result<List<Goods>> list() {
        return Result.success(goodsService.listAll());
    }
}

