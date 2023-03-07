package com.ps.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ps.entity.ShoppingCart;
import com.ps.mapper.ShoppingCartMapper;
import com.ps.service.ShoppingCartService;
import org.springframework.stereotype.Service;

@Service
public class ShoppingCartServiceImpl extends ServiceImpl<ShoppingCartMapper, ShoppingCart> implements ShoppingCartService {
}
