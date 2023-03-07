package com.ps.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ps.common.R;
import com.ps.dto.DishDto;
import com.ps.entity.Category;
import com.ps.entity.Dish;
import com.ps.entity.DishFlavor;
import com.ps.service.CategoryService;
import com.ps.service.DishFlavorService;
import com.ps.service.DishService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Service
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {

        dishService.saveWithFlavor(dishDto);

        return R.success("菜品信息保存成功");
    }

    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize, String name) {
        //分页构造器
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPageInfo = new Page<>();
        //条件构造器
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        //添加查询条件
        lqw.eq(StringUtils.isNotEmpty(name), Dish::getName, name);
        lqw.orderByDesc(Dish::getUpdateTime);
        //进行分页查询
        dishService.page(pageInfo, lqw);
        //spring框架提供的对象拷贝工具类
        BeanUtils.copyProperties(pageInfo, dishDtoPageInfo, "records");
        //通过stream流将dish原本的属性拷贝在新的dishDto中，并且通过查询数据库补全categoryName属性
        List<Dish> dishRecords = pageInfo.getRecords();
        List<DishDto> dishDtoRecords = dishRecords.stream()
                .map((item) -> {
                    DishDto dishDto = new DishDto();
                    BeanUtils.copyProperties(item, dishDto);
                    Long categoryId = item.getCategoryId();
                    Category category = categoryService.getById(categoryId);
                    if (category != null) {
                        dishDto.setCategoryName(category.getName());
                    }
                    return dishDto;
                }).collect(Collectors.toList());
        //将dishDtoPageInfo中的records补全
        dishDtoPageInfo.setRecords(dishDtoRecords);

        return R.success(dishDtoPageInfo);
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        dishService.updateWithFlavors(dishDto);
        return R.success("菜品修改成功");
    }

    @DeleteMapping
    public R<String> deleteById(Long ids) {

        dishService.removeById(ids);

        return R.success("菜品删除成功");
    }

    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable String status, @RequestParam("ids") List<Long> ids) {
        LambdaUpdateWrapper<Dish> luw = new LambdaUpdateWrapper<>();
        luw.set(Dish::getStatus, status);
        for (Long id : ids) {
            luw.eq(Dish::getId, id);
            dishService.update(luw);
        }
        return R.success("菜品更新成功");
    }

    @GetMapping("/{id}")
    public R<DishDto> queryDishById(@PathVariable Long id) {
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    @GetMapping("/list")
    public R<List<DishDto>> list(Long categoryId, String name) {
        LambdaQueryWrapper<Dish> lqw = new LambdaQueryWrapper<>();
        lqw.like(StringUtils.isNotEmpty(name), Dish::getName, name)
                .eq(Dish::getCategoryId, categoryId)
                .eq(Dish::getStatus, 1)
                .orderByDesc(Dish::getUpdateTime);
        List<Dish> dishes = dishService.list(lqw);
        List<DishDto> dishDtos = dishes.stream()
                .map((item) -> {
                    DishDto dishDto = new DishDto();
                    BeanUtils.copyProperties(item, dishDto);
                    Category category = categoryService.getById(categoryId);
                    if (category != null) {
                        dishDto.setCategoryName(category.getName());
                    }
                    LambdaQueryWrapper<DishFlavor> lqw2 = new LambdaQueryWrapper<>();
                    lqw2.eq(DishFlavor::getDishId, item.getId());
                    List<DishFlavor> flavors = dishFlavorService.list(lqw2);
                    dishDto.setFlavors(flavors);
                    return dishDto;
                }).collect(Collectors.toList());
        return R.success(dishDtos);
    }

}
