package com.zhuanzhuan.platform.trade.mapper;

import com.zhuanzhuan.dto.AdminOrderPageQueryDTO;
import com.zhuanzhuan.entity.PayRecord;
import com.zhuanzhuan.vo.AdminOrderDetailVO;
import com.zhuanzhuan.vo.OrderDetailVO;
import com.zhuanzhuan.vo.PayRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PayRecordMapper {

    void insert(PayRecord payRecord);
    // 管理员订单分页
    List<OrderDetailVO> adminPageQuery(@Param("dto") AdminOrderPageQueryDTO dto);

    // 管理员订单详情
    AdminOrderDetailVO adminDetail(Long id);
    List<PayRecordVO> listByOrderId(Long orderId);


    int deleteByOrderId(Long orderId);
}