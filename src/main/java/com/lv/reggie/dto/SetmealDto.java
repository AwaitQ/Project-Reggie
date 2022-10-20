package com.lv.reggie.dto;

import com.lv.reggie.entity.Setmeal;
import com.lv.reggie.entity.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
