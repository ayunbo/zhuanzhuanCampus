package com.zhuanzhuan.platform.goods.mapper;

import com.zhuanzhuan.annotation.AutoFill;
import com.zhuanzhuan.dto.AdminGoodsPageQueryDTO;
import com.zhuanzhuan.dto.GoodsPageQueryDTO;
import com.zhuanzhuan.dto.SellerGoodsPageQueryDTO;
import com.zhuanzhuan.entity.Goods;
import com.zhuanzhuan.enumeration.OperationType;
import com.zhuanzhuan.vo.AdminGoodsDetailVO;
import com.zhuanzhuan.vo.AdminGoodsPageVO;
import com.zhuanzhuan.vo.SellerGoodsDetailVO;
import com.zhuanzhuan.vo.SellerGoodsPageVO;
import com.zhuanzhuan.vo.UserGoodsDetailVO;
import com.zhuanzhuan.vo.UserGoodsPageVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 商品数据访问接口。
 */
@Mapper
public interface GoodsMapper {

    /**
     * 根据商品 ID 查询商品基础信息。
     *
     * @param id 商品 ID
     * @return 商品实体
     */
    @Select("select * from goods where id = #{id} limit 1")
    Goods selectById(Long id);

    /**
     * 兼容订单模块的按 ID 查询方法。
     *
     * @param id 商品 ID
     * @return 商品实体
     */
    Goods getById(Long id);

    /**
     * 根据商品 ID 和卖家 ID 查询商品基础信息。
     *
     * @param id 商品 ID
     * @param sellerId 卖家 ID
     * @return 商品实体
     */
    @Select("select * from goods where id = #{id} and seller_id = #{sellerId} limit 1")
    Goods selectByIdAndSellerId(@Param("id") Long id, @Param("sellerId") Long sellerId);

    /**
     * 根据商品 ID 和卖家 ID 删除商品。
     *
     * @param id 商品 ID
     * @param sellerId 卖家 ID
     * @return 影响行数
     */
    @Delete("delete from goods where id = #{id} and seller_id = #{sellerId}")
    int deleteByIdAndSellerId(@Param("id") Long id, @Param("sellerId") Long sellerId);

    /**
     * 新增商品。
     *
     * @param goods 商品实体
     * @return 影响行数
     */
    @AutoFill(OperationType.INSERT)
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    @Insert("""
            insert into goods (
                seller_id, category_id, title, detail, price, old_price, quality, location, status, cover, reason,
                audit_admin_id, audit_time, publish_time, view_count, favorite_count, lock_order_id, version,
                create_time, update_time, create_user, update_user
            ) values (
                #{sellerId}, #{categoryId}, #{title}, #{detail}, #{price}, #{oldPrice}, #{quality}, #{location}, #{status}, #{cover}, #{reason},
                #{auditAdminId}, #{auditTime}, #{publishTime}, #{viewCount}, #{favoriteCount}, #{lockOrderId}, #{version},
                #{createTime}, #{updateTime}, #{createUser}, #{updateUser}
            )
            """)
    int insert(Goods goods);

    /**
     * 修改商品基础信息。
     *
     * @param goods 商品实体
     * @return 影响行数
     */
    @AutoFill(OperationType.UPDATE)
    @Update("""
            update goods
            set category_id = #{categoryId},
                title = #{title},
                detail = #{detail},
                price = #{price},
                old_price = #{oldPrice},
                quality = #{quality},
                location = #{location},
                cover = #{cover},
                status = #{status},
                reason = #{reason},
                audit_admin_id = #{auditAdminId},
                audit_time = #{auditTime},
                publish_time = #{publishTime},
                version = version + 1,
                update_time = #{updateTime},
                update_user = #{updateUser}
            where id = #{id}
              and seller_id = #{sellerId}
              and version = #{version}
            """)
    int updateById(Goods goods);

    /**
     * 修改商品状态相关字段。
     *
     * @param goods 商品实体
     * @return 影响行数
     */
    @AutoFill(OperationType.UPDATE)
    @Update("""
            update goods
            set status = #{status},
                reason = #{reason},
                audit_admin_id = #{auditAdminId},
                audit_time = #{auditTime},
                publish_time = #{publishTime},
                lock_order_id = #{lockOrderId},
                version = version + 1,
                update_time = #{updateTime},
                update_user = #{updateUser}
            where id = #{id}
              and version = #{version}
            """)
    int updateStatusById(Goods goods);

    /**
     * 增加商品浏览量。
     *
     * @param goodsId 商品 ID
     * @param delta 增量
     * @return 影响行数
     */
    @Update("""
            update goods
            set view_count = view_count + #{delta},
                version = version + 1
            where id = #{goodsId}
            """)
    int increaseViewCount(@Param("goodsId") Long goodsId, @Param("delta") Integer delta);

    /**
     * 调整商品收藏量。
     *
     * @param goodsId 商品 ID
     * @param delta 增量
     * @return 影响行数
     */
    @Update("""
            update goods
            set favorite_count = greatest(0, favorite_count + #{delta}),
                version = version + 1
            where id = #{goodsId}
            """)
    int adjustFavoriteCount(@Param("goodsId") Long goodsId, @Param("delta") Integer delta);

    /**
     * 统计指定卖家指定状态的商品数量。
     */
    @Select("select count(1) from goods where seller_id = #{sellerId} and status = #{status}")
    Long countBySellerIdAndStatus(@Param("sellerId") Long sellerId, @Param("status") Integer status);

    /**
     * 锁定商品，防止同一商品被重复下单。
     */
    int lockGoods(@Param("id") Long id,
                  @Param("oldStatus") Integer oldStatus,
                  @Param("newStatus") Integer newStatus,
                  @Param("lockOrderId") Long lockOrderId);

    /**
     * 释放商品锁定状态。
     */
    int unlockGoods(@Param("id") Long id,
                    @Param("lockOrderId") Long lockOrderId,
                    @Param("oldStatus") Integer oldStatus,
                    @Param("newStatus") Integer newStatus);

    /**
     * 将商品更新为已售出。
     */
    int soldGoods(@Param("id") Long id,
                  @Param("lockOrderId") Long lockOrderId,
                  @Param("oldStatus") Integer oldStatus,
                  @Param("newStatus") Integer newStatus);

    /**
     * 管理端直接调整商品状态。
     */
    int adminSetStatus(@Param("id") Long id,
                       @Param("status") Integer status,
                       @Param("lockOrderId") Long lockOrderId);

    /**
     * 用户端分页查询商品。
     *
     * @param dto 查询条件
     * @return 商品列表
     */
    List<UserGoodsPageVO> pageUser(GoodsPageQueryDTO dto);

    /**
     * 卖家端分页查询商品。
     *
     * @param sellerId 卖家 ID
     * @param dto 查询条件
     * @return 商品列表
     */
    List<SellerGoodsPageVO> pageSeller(@Param("sellerId") Long sellerId, @Param("dto") SellerGoodsPageQueryDTO dto);

    /**
     * 管理端分页查询商品。
     *
     * @param dto 查询条件
     * @return 商品列表
     */
    List<AdminGoodsPageVO> pageAdmin(AdminGoodsPageQueryDTO dto);

    /**
     * 用户端查询商品详情。
     *
     * @param goodsId 商品 ID
     * @return 商品详情
     */
    UserGoodsDetailVO detailUser(Long goodsId);

    /**
     * 卖家端查询商品详情。
     *
     * @param goodsId 商品 ID
     * @param sellerId 卖家 ID
     * @return 商品详情
     */
    SellerGoodsDetailVO detailSeller(@Param("goodsId") Long goodsId, @Param("sellerId") Long sellerId);

    /**
     * 管理端查询商品详情。
     *
     * @param goodsId 商品 ID
     * @return 商品详情
     */
    AdminGoodsDetailVO detailAdmin(Long goodsId);
}
