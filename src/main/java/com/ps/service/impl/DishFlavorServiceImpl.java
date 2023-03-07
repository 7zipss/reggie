package com.ps.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ps.mapper.DishFlavorMapper;
import com.ps.entity.DishFlavor;
import com.ps.service.DishFlavorService;
import org.springframework.stereotype.Service;

@Service
public class DishFlavorServiceImpl extends ServiceImpl<DishFlavorMapper, DishFlavor> implements DishFlavorService {
}
