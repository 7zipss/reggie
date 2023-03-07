package com.ps.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ps.common.R;
import com.ps.dto.SetmealDto;
import com.ps.entity.Category;
import com.ps.entity.Dish;
import com.ps.entity.Setmeal;
import com.ps.entity.SetmealDish;
import com.ps.service.CategoryService;
import com.ps.service.SetmealDishService;
import com.ps.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    public R<List<SetmealDto>> list(Long categoryId, Integer status){
        //条件构造器查找对应分类下的套餐集合
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.eq(Setmeal::getCategoryId, categoryId)
                .eq(Setmeal::getStatus, status);
        List<Setmeal> setmeals = setmealService.list(lqw);
        //查找此分类的分类名称，在stream流中为setmealDto中的属性赋值
        Category category = categoryService.getById(categoryId);
        String categoryName = category.getName();
        //通过stream流将Setmeal转换成包含dishs和分类名称的Dto
        List<SetmealDto> setmealDtos = setmeals.stream()
                .map((item) -> {
                    //创建空对象，并进行属性拷贝
                    SetmealDto setmealDto = new SetmealDto();
                    BeanUtils.copyProperties(item, setmealDto);
                    //为其他属性赋值
                    setmealDto.setCategoryName(categoryName);
                    //查找此套餐对应的菜品集合，并赋值
                    Long setmealId = item.getId();
                    LambdaQueryWrapper<SetmealDish> lqw2 = new LambdaQueryWrapper<>();
                    lqw2.eq(SetmealDish::getSetmealId, setmealId);
                    List<SetmealDish> setmealDishes = setmealDishService.list(lqw2);
                    setmealDto.setSetmealDishes(setmealDishes);
                    //返回构造好的Dto
                    return setmealDto;
                }).collect(Collectors.toList());
        return R.success(setmealDtos);
    }

    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable String status, @RequestParam("ids") List<Long> ids){
        LambdaUpdateWrapper<Setmeal> luw = new LambdaUpdateWrapper<>();
        luw.set(Setmeal::getStatus, status);
        for (Long id : ids) {
            luw.eq(Setmeal::getId, id);
            setmealService.update(luw);
        }
        return R.success("套餐状态修改成功");
    }

    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateById(setmealDto);
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream()
                .map((item) -> {
                    item.setSetmealId(setmealDto.getId());
                    return item;
                }).collect(Collectors.toList());
        //删除套餐对应的dishs
        LambdaQueryWrapper<SetmealDish> lqw = new LambdaQueryWrapper<>();
        lqw.eq(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(lqw);
        //保存此套餐对应的新dishs
        setmealDishService.saveBatch(setmealDishes);
        return R.success("套餐修改成功");
    }

    @DeleteMapping
    public R<String> delete(Long ids){
        LambdaQueryWrapper<SetmealDish> lqw1 = new LambdaQueryWrapper<>();
        lqw1.eq(SetmealDish::getSetmealId, ids);
        setmealDishService.remove(lqw1);

        LambdaQueryWrapper<Setmeal> lqw2 = new LambdaQueryWrapper<>();
        lqw2.eq(Setmeal::getId, ids);
        setmealService.remove(lqw2);
        return R.success("套餐删除成功");
    }

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.saveSetmealWithDish(setmealDto);
        return R.success("新建套餐成功");
    }

    @GetMapping("/{id}")
    public R<SetmealDto> getSetmealById(@PathVariable Long id){
        SetmealDto setmealDto = setmealService.getSetmealByIdWithDish(id);
        return R.success(setmealDto);
    }

    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize, String name){
        //构建分页构造器
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> newPageInfo = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Setmeal> lqw = new LambdaQueryWrapper<>();
        lqw.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);
        setmealService.page(pageInfo, lqw);
        BeanUtils.copyProperties(pageInfo, newPageInfo, "records");
        List<Setmeal> records = pageInfo.getRecords();
        //在基础属性完整的情况下， 通过stream流补充完整categoryName
        List<SetmealDto> newRecords = records.stream()
                .map((item)->{
                    SetmealDto setmealDto = new SetmealDto();
                    BeanUtils.copyProperties(item, setmealDto);
                    Long categoryId = item.getCategoryId();
                    Category category = categoryService.getById(categoryId);
                    setmealDto.setCategoryName(category.getName());
                    return setmealDto;
                }).collect(Collectors.toList());
        newPageInfo.setRecords(newRecords);
        return R.success(newPageInfo);
    }

}
