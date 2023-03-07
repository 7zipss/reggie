package com.ps.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ps.dto.SetmealDto;
import com.ps.entity.Setmeal;
import com.ps.entity.SetmealDish;
import com.ps.mapper.SetmealMapper;
import com.ps.service.SetmealDishService;
import com.ps.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {

    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    public void saveSetmealWithDish(SetmealDto setmealDto) {
        //保存基本参数
        this.save(setmealDto);
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        Long id = setmealDto.getId();
        setmealDishes = setmealDishes.stream()
                .map((item)->{
                    item.setSetmealId(id);
                    return item;
                }).collect(Collectors.toList());
        //保存补充dishId后的数据到setmeal_dish表
        setmealDishService.saveBatch(setmealDishes);
    }

    @Override
    public SetmealDto getSetmealByIdWithDish(Long id) {
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        //补充完整基本参数
        BeanUtils.copyProperties(setmeal, setmealDto);
        //查表并补充dish参数
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getDishId, id);
        List<SetmealDish> setmealDishes = setmealDishService.list(lqw);
        setmealDto.setSetmealDishes(setmealDishes);
        return setmealDto;
    }
}
