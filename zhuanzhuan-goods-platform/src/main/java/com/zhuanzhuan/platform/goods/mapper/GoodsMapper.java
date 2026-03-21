package com.zhuanzhuan.platform.goods.mapper;

import com.zhuanzhuan.annotation.AutoFill;
import com.zhuanzhuan.dto.AdminGoodsPageQueryDTO;
import com.zhuanzhuan.dto.GoodsPageQueryDTO;
import com.zhuanzhuan.entity.Goods;
import com.zhuanzhuan.enumeration.OperationType;
import com.zhuanzhuan.vo.GoodsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GoodsMapper {

    Goods getById(Long id);

    Goods getByIdAndSellerId(@Param("id") Long id, @Param("sellerId") Long sellerId);

    @AutoFill(OperationType.INSERT)
    int insert(Goods goods);

    @AutoFill(OperationType.UPDATE)
    int updateDraftById(Goods goods);

    @AutoFill(OperationType.UPDATE)
    int updateLifecycleById(Goods goods);

    List<GoodsVO> pageBySeller(@Param("sellerId") Long sellerId, @Param("status") Integer status);

    List<GoodsVO> adminPage(AdminGoodsPageQueryDTO pageQueryDTO);

    List<GoodsVO> pageForUser(GoodsPageQueryDTO pageQueryDTO);

    GoodsVO getDetailById(Long id);

    int updateStats(@Param("goodsId") Long goodsId,
                    @Param("viewDelta") Integer viewDelta,
                    @Param("favoriteDelta") Integer favoriteDelta);
}
