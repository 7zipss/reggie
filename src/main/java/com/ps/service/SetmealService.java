package com.ps.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ps.dto.SetmealDto;
import com.ps.entity.Setmeal;

public interface SetmealService extends IService<Setmeal> {

    public void saveSetmealWithDish(SetmealDto setmealDto);

    public SetmealDto getSetmealByIdWithDish(Long id);

}
