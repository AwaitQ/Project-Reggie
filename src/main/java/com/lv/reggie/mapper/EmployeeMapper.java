package com.lv.reggie.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lv.reggie.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}
