package com.ps.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ps.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
