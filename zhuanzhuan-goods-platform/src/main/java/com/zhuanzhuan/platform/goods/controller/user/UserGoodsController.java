package com.zhuanzhuan.platform.goods.controller.user;

import com.zhuanzhuan.constant.JwtClaimsConstant;
import com.zhuanzhuan.context.BaseContext;
import com.zhuanzhuan.dto.GoodsPageQueryDTO;
import com.zhuanzhuan.platform.goods.service.UserGoodsService;
import com.zhuanzhuan.platform.history.service.BrowseHistoryService;
import com.zhuanzhuan.properties.JwtProperties;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.result.Result;
import com.zhuanzhuan.utils.JwtUtil;
import com.zhuanzhuan.vo.SellerSpaceVO;
import com.zhuanzhuan.vo.UserGoodsDetailVO;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
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

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private BrowseHistoryService browseHistoryService;

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
     * 查询卖家空间基础信息。
     *
     * @param sellerId 卖家 ID
     * @return 卖家空间信息
     */
    @GetMapping("/seller/{sellerId:\\d+}")
    public Result<SellerSpaceVO> sellerSpace(@PathVariable Long sellerId) {
        return Result.success(userGoodsService.getSellerSpace(sellerId));
    }

    /**
     * 用户端查询商品详情。
     *
     * @param id 商品 ID
     * @param request 请求对象
     * @return 商品详情
     */
    @GetMapping("/{id:\\d+}")
    public Result<UserGoodsDetailVO> detail(@PathVariable Long id, HttpServletRequest request) {
        bindCurrentUserIfPresent(request);
        try {
            UserGoodsDetailVO detailVO = userGoodsService.getDetail(id);
            recordHistoryQuietly(id);
            return Result.success(detailVO);
        } finally {
            BaseContext.removeCurrentId();
        }
    }

    private void bindCurrentUserIfPresent(HttpServletRequest request) {
        String token = request.getHeader(jwtProperties.getUserTokenName());
        if (!StringUtils.hasText(token)) {
            return;
        }

        try {
            Claims claims = JwtUtil.parseJWT(jwtProperties.getUserSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.USER_ID).toString());
            BaseContext.setCurrentId(userId);
        } catch (Exception ex) {
            BaseContext.removeCurrentId();
        }
    }

    private void recordHistoryQuietly(Long goodsId) {
        if (BaseContext.getCurrentId() == null) {
            return;
        }
        try {
            browseHistoryService.record(goodsId);
        } catch (Exception ignored) {
            // 浏览历史记录失败不影响详情主流程
        }
    }
}
