package com.lv.reggie.dto;

import com.lv.reggie.entity.Orders;
import lombok.Data;

@Data
public class OrdersDto extends Orders {
    private Integer sumNum;
}
