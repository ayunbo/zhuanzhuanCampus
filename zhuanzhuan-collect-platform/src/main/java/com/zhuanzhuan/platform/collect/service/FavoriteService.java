package com.zhuanzhuan.platform.collect.service;

import com.zhuanzhuan.dto.FavoritePageQueryDTO;
import com.zhuanzhuan.result.PageResult;
import com.zhuanzhuan.vo.FavoriteStatusVO;
import com.zhuanzhuan.vo.FavoriteToggleVO;

public interface FavoriteService {

    FavoriteToggleVO collect(Long goodsId);

    FavoriteToggleVO cancel(Long goodsId);

    FavoriteStatusVO status(Long goodsId);

    PageResult page(FavoritePageQueryDTO dto);
}
