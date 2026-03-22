package com.zhuanzhuan.platform.trade.mapper;

import com.zhuanzhuan.dto.AdminOrderPageQueryDTO;
import com.zhuanzhuan.dto.OrderPageQueryDTO;
import com.zhuanzhuan.entity.Order;
import com.zhuanzhuan.vo.AdminOrderDetailVO;
import com.zhuanzhuan.vo.OrderDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper {

    void insert(Order order);

    Order getById(Long id);

    Order getByIdAndBuyerId(@Param("id") Long id, @Param("buyerId") Long buyerId);

    int cancelOrder(@Param("id") Long id,
                    @Param("buyerId") Long buyerId,
                    @Param("oldStatus") Integer oldStatus,
                    @Param("newStatus") Integer newStatus,
                    @Param("closeTime") LocalDateTime closeTime);

    int completeOrder(@Param("id") Long id,
                      @Param("buyerId") Long buyerId,
                      @Param("oldStatus") Integer oldStatus,
                      @Param("newStatus") Integer newStatus,
                      @Param("completeTime") LocalDateTime completeTime);

    int paySuccess(@Param("id") Long id,
                   @Param("oldStatus") Integer oldStatus,
                   @Param("newStatus") Integer newStatus,
                   @Param("payTime") LocalDateTime payTime);

    List<Order> listTimeoutOrders(LocalDateTime now);

    int timeoutCloseOrder(@Param("id") Long id,
                          @Param("oldStatus") Integer oldStatus,
                          @Param("newStatus") Integer newStatus,
                          @Param("closeTime") LocalDateTime closeTime);

    OrderDetailVO getDetailById(Long id);

    List<OrderDetailVO> pageQueryBuy(@Param("dto") OrderPageQueryDTO dto,
                                     @Param("buyerId") Long buyerId);

    List<OrderDetailVO> pageQuerySell(@Param("dto") OrderPageQueryDTO dto,
                                      @Param("sellerId") Long sellerId);

    List<OrderDetailVO> adminPageQuery(@Param("dto") AdminOrderPageQueryDTO dto);

    AdminOrderDetailVO adminDetail(Long id);

    int adminUpdateStatus(@Param("id") Long id, @Param("status") Integer status);

    int adminSetPayTime(@Param("id") Long id, @Param("payTime") LocalDateTime payTime);

    int adminSetCompleteTime(@Param("id") Long id, @Param("completeTime") LocalDateTime completeTime);

    int adminSetCloseTime(@Param("id") Long id, @Param("closeTime") LocalDateTime closeTime);

    int deleteById(Long id);
}
