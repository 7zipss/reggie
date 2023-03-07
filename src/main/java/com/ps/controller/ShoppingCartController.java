package com.ps.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ps.common.BaseContext;
import com.ps.common.R;
import com.ps.entity.ShoppingCart;
import com.ps.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @DeleteMapping("/clean")
    public R<String> clean(){
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, currentId);
        shoppingCartService.remove(lqw);
        return R.success("清空购物车成功");
    }

    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart){
        //只操作此用户
        Long currentId = BaseContext.getCurrentId();
        //查询此购物车项原数量有多少
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId())
                .eq(ShoppingCart::getUserId, currentId)
                .eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, shoppingCart.getDishId());
        ShoppingCart shoppingCart1 = shoppingCartService.getOne(lqw);
        Integer number = shoppingCart1.getNumber();
        Long id = shoppingCart1.getId();
        //得到number并判断number是否为1
        if(number == 1){//如果为1则直接删除此项
            shoppingCartService.removeById(id);
        } else {//将此购物项数量-1
            LambdaUpdateWrapper<ShoppingCart> luw = new LambdaUpdateWrapper<>();
            luw.eq(ShoppingCart::getId, id)
                    .set(ShoppingCart::getNumber, number - 1);
            shoppingCartService.update(luw);
        }
        return R.success("购物车数量减一成功");
    }

    @PostMapping("/add")
    public R<String> add(@RequestBody ShoppingCart shoppingCart){
        //只操作此用户
        Long currentId = BaseContext.getCurrentId();
        //如果购物车中对应用户id查询出的数据中已经存在了要add的数据
        //说明此次add是增加商品数量
        //如果不存在则是添加新的商品到购物车
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, shoppingCart.getDishId())
                .eq(ShoppingCart::getUserId, currentId)
                .eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        ShoppingCart shoppingCart1 = shoppingCartService.getOne(lqw);
        if(shoppingCart1 != null){//已存在此商品, 只需要数量+1
            shoppingCart1.setNumber(shoppingCart1.getNumber() + 1);
            shoppingCartService.updateById(shoppingCart1);
        } else {//新的商品，补充好userId字段后添加到数据库
            shoppingCart.setUserId(currentId);
            shoppingCartService.save(shoppingCart);
        }
        return R.success("保存购物车成功");
    }

    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, currentId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(lqw);
        return R.success(shoppingCarts);
    }



}
