package com.ps.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ps.entity.User;
import com.ps.mapper.UserMapper;
import com.ps.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}
