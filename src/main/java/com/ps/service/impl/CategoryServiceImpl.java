package com.ps.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ps.mapper.CategoryMapper;
import com.ps.entity.Category;
import com.ps.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
}
