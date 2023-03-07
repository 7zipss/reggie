package com.ps.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ps.mapper.EmployeeMapper;
import com.ps.entity.Employee;
import com.ps.service.EmployeeService;
import org.springframework.stereotype.Service;

@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}
