package com.zhuanzhuan.platform.trade.mapper;

import com.zhuanzhuan.entity.Pay;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

@Mapper
public interface PayMapper {

    void insert(Pay pay);

    Pay getByOrderId(Long orderId);

    Pay getByRequestNo(String requestNo);

    int updateLaunchInfo(@Param("id") Long id,
                         @Param("requestNo") String requestNo,
                         @Param("method") Integer method,
                         @Param("updateUser") Long updateUser);

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

    int adminUpdateStatus(@Param("orderId") Long orderId,
                          @Param("status") Integer status,
                          @Param("payTime") LocalDateTime payTime);

    int deleteByOrderId(Long orderId);
}
