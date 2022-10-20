package com.lv.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lv.reggie.entity.ShoppingCart;
import com.lv.reggie.mapper.ShoppingCarMapper;
import com.lv.reggie.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCarServiceImpl extends ServiceImpl<ShoppingCarMapper, ShoppingCart> implements ShoppingCartService {
}
