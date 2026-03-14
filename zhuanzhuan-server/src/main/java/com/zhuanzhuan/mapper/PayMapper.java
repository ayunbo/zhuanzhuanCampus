package com.zhuanzhuan.mapper;

import com.zhuanzhuan.entity.Pay;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface PayMapper {

    void insert(Pay pay);

     Pay getByOrderId(Long orderId);

    int paySuccess(@Param("id") Long id,
                   @Param("oldStatus") Integer oldStatus,
                   @Param("newStatus") Integer newStatus,
                   @Param("payTime") LocalDateTime payTime);

     int closePayByOrderId(@Param("orderId") Long orderId,
                                 @Param("oldStatus") Integer oldStatus,
                                 @Param("newStatus") Integer newStatus);

     int adminPaySuccess(@Param("orderId") Long orderId,
                               @Param("status") Integer status,
                               @Param("payTime") LocalDateTime payTime);

    int deleteByOrderId(Long orderId);
}