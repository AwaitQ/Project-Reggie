package com.lv.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lv.reggie.common.BaseContext;
import com.lv.reggie.common.R;
import com.lv.reggie.dto.DishDto;
import com.lv.reggie.dto.OrdersDto;
import com.lv.reggie.entity.Category;
import com.lv.reggie.entity.Dish;
import com.lv.reggie.entity.OrderDetail;
import com.lv.reggie.entity.Orders;
import com.lv.reggie.service.OrderDetailService;
import com.lv.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单
 */
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("订单数据：{}", orders);
        orderService.submit(orders);
        return R.success("用户下单成功！");

    }

    /**
     * 菜品信息分页查询
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number, String beginTime, String endTime) throws ParseException {
        log.info(String.valueOf(beginTime));
        log.info(String.valueOf(endTime));
        Date beginTimeDate = new Date();
        Date endTimeDate = new Date();

        if(beginTime != null) {
            beginTimeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(beginTime);
        }
        if(endTime != null) {
            endTimeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(endTime);
        }

        //构造分页构造器对象
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        //条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(number != null, Orders::getNumber, number);
        if(beginTime != null && endTime != null) {
            queryWrapper.between(Orders::getOrderTime, beginTimeDate, endTimeDate);
        }

        //添加排序条件
        queryWrapper.orderByDesc(Orders::getOrderTime);

        //执行分页查询
        orderService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 派送订单
     * @param orders
     * @return
     */
    @PutMapping
    public R<String> delivery(@RequestBody Orders orders) {
        log.info(String.valueOf(orders));
        orderService.updateById(orders);

        return R.success("成功派送订单！");
    }

    /**
     * 获取最新订单
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> userPage(int page, int pageSize) {
        log.info("页数：{}，每页条数：{}", page, pageSize);
        //获取当前用户id
        Long userId = BaseContext.getCurrentId();
        //构造分页构造器对象
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>(page, pageSize);
        //条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper();
        //添加条件
        queryWrapper.eq(Orders::getUserId, userId);
        //添加排序条件
        queryWrapper.orderByDesc(Orders::getOrderTime);

        //获取订单
        orderService.page(pageInfo, queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo, ordersDtoPage, "records");

        //重新组装订单数据
        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> list = records.stream().map((item) -> {

            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);

            Long orderId = item.getId();  //订单id
            LambdaQueryWrapper<OrderDetail> lambdaQueryWrapper = new LambdaQueryWrapper();
            lambdaQueryWrapper.eq(OrderDetail::getOrderId, orderId);
            int count = orderDetailService.count(lambdaQueryWrapper);//根据订单id查询菜品数量
            ordersDto.setSumNum(count);

            return ordersDto;
        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(list);

        return R.success(ordersDtoPage);

    }
}
