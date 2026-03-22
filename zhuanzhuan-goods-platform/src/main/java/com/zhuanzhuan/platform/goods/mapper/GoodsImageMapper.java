package com.zhuanzhuan.platform.goods.mapper;

import com.zhuanzhuan.entity.GoodsImage;
import com.zhuanzhuan.vo.GoodsImageVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品图片数据访问接口。
 */
@Mapper
public interface GoodsImageMapper {

    /**
     * 根据商品 ID 查询图片列表。
     *
     * @param goodsId 商品 ID
     * @return 图片列表
     */
    @Select("""
            select url, sort, is_cover as isCover
            from goodsimage
            where goods_id = #{goodsId}
            order by sort asc, id asc
            """)
    List<GoodsImageVO> selectByGoodsId(Long goodsId);

    /**
     * 根据商品 ID 删除图片列表。
     *
     * @param goodsId 商品 ID
     * @return 影响行数
     */
    @Delete("delete from goodsimage where goods_id = #{goodsId}")
    int deleteByGoodsId(Long goodsId);

    /**
     * 批量新增商品图片。
     *
     * @param goodsImages 图片列表
     * @return 影响行数
     */
    int insertBatch(@Param("list") List<GoodsImage> goodsImages);
}
