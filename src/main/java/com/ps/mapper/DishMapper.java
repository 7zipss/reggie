package com.ps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ps.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
