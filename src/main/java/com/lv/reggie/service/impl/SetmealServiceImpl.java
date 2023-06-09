package com.lv.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lv.reggie.common.CustomException;
import com.lv.reggie.dto.SetmealDto;
import com.lv.reggie.entity.Dish;
import com.lv.reggie.entity.DishFlavor;
import com.lv.reggie.entity.Setmeal;
import com.lv.reggie.entity.SetmealDish;
import com.lv.reggie.mapper.SetMealMapper;
import com.lv.reggie.service.SetmealDishService;
import com.lv.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetMealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */

    @Transactional
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐的基本信息，操作setmeal，执行insert操作
        this.save(setmealDto);

        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //保存套餐和菜品的关联关系，操作setmeal_dish，执行insert操作
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐，同时删除套餐和菜品的关联数据
     * @param ids
     */
    @Transactional
    @Override
    public void removeWithDish(List<Long> ids) {
        //查询套餐状态，确定是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(Setmeal::getId, ids);
        queryWrapper.eq(Setmeal::getStatus, 1);

        //数据库查询
        int count = this.count(queryWrapper);
        if (count > 0) {
            //如果不能删除，抛出一个业务异常
            throw new CustomException("套餐正在售卖中，不能删除！");
        }

        //如果可以删除，先删除套餐表中的数据---setmeal
        this.removeByIds(ids);

        //删除关系表中的数据---setmeal_dish
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);

        setmealDishService.remove(lambdaQueryWrapper);

    }

    /**
     * 批量停售套餐
     * @param status
     * @param ids
     */
    @Transactional
    public void haltSalesWithDish(Integer status, List<Long> ids) {
        List<Setmeal> setmealList = new LinkedList<>();

        //查询对应的菜单列表，并对启售状态进行更新
        for (Long id : ids) {
            Setmeal setmeal = this.getById(id);
            setmeal.setStatus(status);
            setmealList.add(setmeal);
        }
        this.updateBatchById(setmealList);

    }

    /**
     * 获得套餐和对应菜品信息
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        //查询套餐基本信息，setmeal表查询
        Setmeal setmeal = this.getById(id);

        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);

        //查询当前套餐对应的菜品信息，从setmeal_dish表查询
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(SetmealDish::getSetmealId, setmeal.getId());
        List<SetmealDish> dishList = setmealDishService.list(queryWrapper);
        setmealDto.setSetmealDishes(dishList);
        return setmealDto;
    }

    /**
     * 修改套餐和对应菜品信息
     */
    @Override
    public void updateWithDish(SetmealDto setmealDto) {
        //更新套餐基本信息
        this.updateById(setmealDto);

        //清理当前套餐对应菜品数据---setmeal_dish表的delete操作
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(SetmealDish::getSetmealId, setmealDto.getId());

        setmealDishService.remove(queryWrapper);

        //添加当前提交过来的菜品数据---setmeal_dish表的insert操作
        List<SetmealDish> dishes = setmealDto.getSetmealDishes();
        dishes = dishes.stream().map((item) -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());
        setmealDishService.saveBatch(dishes);
    }
}
