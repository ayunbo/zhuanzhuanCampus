package com.zhuanzhuan.service;

import com.zhuanzhuan.entity.Goods;

import java.util.List;

public interface GoodsService {
    Goods getById(Long id);


    List<Goods> listAll();
}