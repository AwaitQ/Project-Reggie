package com.lv.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lv.reggie.dto.SetmealDto;
import com.lv.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐，同时删除套餐和菜品的关联数据
     * @param ids
     */
    public void removeWithDish(List<Long> ids);

    /**
     * 批量停售套餐
     * @param status
     * @param ids
     */
    public void haltSalesWithDish(Integer status, List<Long> ids);

    /**
     * 获得套餐和对应菜品信息
     * @param id
     * @return
     */
    public SetmealDto getByIdWithDish(Long id);

    /**
     * 修改对应套餐和菜品信息
     */
    public void updateWithDish(SetmealDto setmealDto);
}
