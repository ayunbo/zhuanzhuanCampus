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

    @Select("""
            select id, goods_id, url, sort, is_cover, create_time, update_time, create_user, update_user
            from goodsimage
            where goods_id = #{goodsId}
            order by sort asc, id asc
            """)
    List<GoodsImage> selectEntitiesByGoodsId(Long goodsId);

    @Select("""
            select distinct goods_id
            from goodsimage
            where url = #{url}
            """)
    List<Long> selectGoodsIdsByUrl(String url);

    @Select("""
            select url
            from goodsimage
            where goods_id = #{goodsId}
            order by sort asc, id asc
            limit 1
            """)
    String selectFirstUrlByGoodsId(Long goodsId);

    /**
     * 根据商品 ID 删除图片列表。
     *
     * @param goodsId 商品 ID
     * @return 影响行数
     */
    @Delete("delete from goodsimage where goods_id = #{goodsId}")
    int deleteByGoodsId(Long goodsId);

    @Delete("delete from goodsimage where goods_id = #{goodsId} and url = #{url}")
    int deleteByGoodsIdAndUrl(@Param("goodsId") Long goodsId, @Param("url") String url);

    @Delete("delete from goodsimage where url = #{url}")
    int deleteByUrl(String url);

    /**
     * 批量新增商品图片。
     *
     * @param goodsImages 图片列表
     * @return 影响行数
     */
    int insertBatch(@Param("list") List<GoodsImage> goodsImages);
}
