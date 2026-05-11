package com.zhuanzhuan.platform.trade.mapper;

import com.zhuanzhuan.dto.AdminOrderPageQueryDTO;
import com.zhuanzhuan.entity.PayRecord;
import com.zhuanzhuan.vo.AdminOrderDetailVO;
import com.zhuanzhuan.vo.OrderDetailVO;
import com.zhuanzhuan.vo.PayRecordVO;
import com.zhuanzhuan.vo.WalletTransactionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PayRecordMapper {

    void insert(PayRecord payRecord);

    List<OrderDetailVO> adminPageQuery(@Param("dto") AdminOrderPageQueryDTO dto);

    AdminOrderDetailVO adminDetail(Long id);

    List<PayRecordVO> listByOrderId(Long orderId);

    List<WalletTransactionVO> pageWalletRecords(@Param("buyerId") Long buyerId);

    int deleteByOrderId(Long orderId);
}
