package com.ps.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ps.dto.DishDto;
import com.ps.entity.Dish;

public interface DishService extends IService<Dish> {

    //新增菜品，同时插入菜品的口味，需要操作dish与dish_flavor两张表
    public void saveWithFlavor(DishDto dishDto);
    //通过id查询带有口味的dishDto
    public DishDto getByIdWithFlavor(Long id);

    void updateWithFlavors(DishDto dishDto);
}
