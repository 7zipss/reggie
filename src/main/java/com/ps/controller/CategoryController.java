package com.ps.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ps.common.R;
import com.ps.entity.Category;
import com.ps.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 保存不同菜品分类， 或者套餐分类
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        categoryService.save(category);
        return R.success(category.getName() + ":保存成功");
    }

    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize){
        //分页构造器
        Page pageInfo = new Page(page, pageSize);
        //条件构造器
        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        //增加条件
        lqw.orderByAsc(Category::getSort);

        categoryService.page(pageInfo, lqw);

        return R.success(pageInfo);
    }

    @DeleteMapping
    public R<String> deleteById(Long ids){
        categoryService.removeById(ids);
        return R.success("删除分类成功");
    }

    @PutMapping
    public R<String> updateById(@RequestBody Category category){
        categoryService.updateById(category);
        return R.success("分类信息成功");
    }

    @GetMapping("/list")
    public R<List<Category>> list(Integer type){

        LambdaQueryWrapper<Category> lqw = new LambdaQueryWrapper<>();
        lqw.eq(type != null, Category::getType, type);
        List<Category> list = categoryService.list(lqw);

        return R.success(list);
    }
}
