package com.zhuanzhuan.mapper;

import com.zhuanzhuan.entity.OrderSnapshot;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderSnapshotMapper {

    void insert(OrderSnapshot orderSnapshot);

    OrderSnapshot getByOrderId(Long orderId);
}