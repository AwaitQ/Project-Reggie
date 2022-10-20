package com.lv.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lv.reggie.entity.OrderDetail;
import com.lv.reggie.entity.Orders;

public interface OrderService extends IService<Orders> {
    void submit(Orders orders);
}
