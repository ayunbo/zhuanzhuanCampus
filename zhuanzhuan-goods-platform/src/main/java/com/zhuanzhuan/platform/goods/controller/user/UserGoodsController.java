package com.zhuanzhuan.platform.goods.controller.user;

import com.zhuanzhuan.dto.GoodsPageQueryDTO;
import com.zhuanzhuan.platform.goods.service.UserGoodsService;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.vo.UserGoodsDetailVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端商品控制器。
 */
@RestController
@RequestMapping("/user/goods")
public class UserGoodsController {

    @Autowired
    private UserGoodsService userGoodsService;

    /**
     * 用户端分页查询商品列表。
     *
     * @param dto 查询条件
     * @return 分页结果
     */
    @GetMapping({"", "/page"})
    public Result<PageResult> page(GoodsPageQueryDTO dto) {
        return Result.success(userGoodsService.page(dto));
    }

    /**
     * 用户端查询商品详情。
     *
     * @param id 商品 ID
     * @return 商品详情
     */
    @GetMapping("/{id:\\d+}")
    public Result<UserGoodsDetailVO> detail(@PathVariable Long id) {
        return Result.success(userGoodsService.getDetail(id));
    }
}
