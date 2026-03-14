package com.zhuanzhuan.mapper;

import com.zhuanzhuan.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GoodsMapper {

    Goods getById(Long id);

    int lockGoods(@Param("id") Long id,
                  @Param("oldStatus") Integer oldStatus,
                  @Param("newStatus") Integer newStatus,
                  @Param("lockOrderId") Long lockOrderId);

    int unlockGoods(@Param("id") Long id,
                    @Param("lockOrderId") Long lockOrderId,
                    @Param("oldStatus") Integer oldStatus,
                    @Param("newStatus") Integer newStatus);

    int soldGoods(@Param("id") Long id,
                  @Param("lockOrderId") Long lockOrderId,
                  @Param("oldStatus") Integer oldStatus,
                  @Param("newStatus") Integer newStatus);

    int adminSetStatus(@Param("id") Long id,
                       @Param("status") Integer status,
                       @Param("lockOrderId") Long lockOrderId);

    List<Goods> listAll();
}
