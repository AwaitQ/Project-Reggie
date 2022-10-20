package com.lv.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lv.reggie.entity.ShoppingCart;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ShoppingCarMapper extends BaseMapper<ShoppingCart> {
}
