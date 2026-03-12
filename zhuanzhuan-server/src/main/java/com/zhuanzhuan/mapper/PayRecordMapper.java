package com.zhuanzhuan.mapper;

import com.zhuanzhuan.entity.PayRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PayRecordMapper {

    void insert(PayRecord payRecord);

    List<PayRecord> listByOrderId(Long orderId);
}