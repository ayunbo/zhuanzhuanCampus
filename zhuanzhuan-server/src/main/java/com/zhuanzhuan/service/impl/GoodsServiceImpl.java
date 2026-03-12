package com.zhuanzhuan.service.impl;

import com.zhuanzhuan.entity.Goods;
import com.zhuanzhuan.mapper.GoodsMapper;
import com.zhuanzhuan.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public Goods getById(Long id) {
        return goodsMapper.getById(id);
    }
}