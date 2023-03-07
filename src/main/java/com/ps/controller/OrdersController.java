package com.ps.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ps.common.BaseContext;
import com.ps.common.R;
import com.ps.dto.OrdersDto;
import com.ps.entity.*;
import com.ps.service.*;
import com.sun.org.apache.xpath.internal.operations.Or;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/order")
@Slf4j
public class OrdersController {

    @Autowired
    private OrdersService ordersService;
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private AddressBookService addressBookService;
    @Autowired
    private UserService userService;

    @PutMapping
    public R<String> updateStatus(@RequestBody Orders orders){
        Long id = orders.getId();
        Integer status = orders.getStatus();
        LambdaUpdateWrapper<Orders> luw = new LambdaUpdateWrapper<>();
        luw.eq(Orders::getId, id)
                .set(Orders::getStatus, status);
        ordersService.update(luw);
        return R.success("订单状态修改成功");
    }

    @GetMapping("/page")
    public R<Page> page(Integer page, Integer pageSize, Long number, String beginTime, String endTime) throws ParseException {
        return defaultPage(page, pageSize, number, beginTime, endTime);
    }

    @GetMapping("/userPage")
    public R<Page> userPage(Integer page, Integer pageSize) throws ParseException {
        return defaultPage(page, pageSize, null, null, null);
    }

    /**
     *  简化代码
     */
    private R<Page> defaultPage(Integer page, Integer pageSize, Long number, String beginTime, String endTime) throws ParseException {
        //分页构造器
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>();
        Date begin = null;
        Date end = null;
        if(StringUtils.isNotEmpty(beginTime) && StringUtils.isNotEmpty(endTime)){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            begin = sdf.parse(beginTime);
            end = sdf.parse(endTime);
        }
        //条件构造器
        LambdaQueryWrapper<Orders> lqw = new LambdaQueryWrapper<>();
        lqw.eq(number != null, Orders::getNumber, number)
                .ge(beginTime != null, Orders::getOrderTime, begin)
                .le(endTime != null, Orders::getOrderTime, end)
                .orderByDesc(Orders::getCheckoutTime);
        ordersService.page(pageInfo, lqw);
        //属性复制
        BeanUtils.copyProperties(pageInfo, ordersDtoPage, "records");
        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> newRecords = records.stream()
                .map((item) -> {
                    //创建Dto对象
                    OrdersDto ordersDto = new OrdersDto();
                    //复制已有属性
                    BeanUtils.copyProperties(item, ordersDto);
                    //填充扩展属性
                    Long userId = item.getUserId();
                    User user = userService.getById(userId);
                    ordersDto.setUserName(user.getName());
                    ordersDto.setConsignee(user.getName());
                    ordersDto.setPhone(user.getPhone());
                    //查询并设置orderDetails
                    Long ordersId = ordersDto.getId();
                    LambdaQueryWrapper<OrderDetail> lqw2 = new LambdaQueryWrapper<>();
                    lqw2.eq(OrderDetail::getOrderId, ordersId);
                    List<OrderDetail> orderDetails = orderDetailService.list(lqw2);
                    ordersDto.setOrderDetails(orderDetails);
                    //返回Dto
                    return ordersDto;
                }).collect(Collectors.toList());
        ordersDtoPage.setRecords(newRecords);
        return R.success(ordersDtoPage);
    }

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        //前端只传入了地址Id、支付方式、备注三个属性，需要我们根据currentId自行补充其他属性
        Long userId = BaseContext.getCurrentId();
        User user = userService.getById(userId);
        //查询用户的购物车信息
        LambdaQueryWrapper<ShoppingCart> lqw = new LambdaQueryWrapper<>();
        lqw.eq(ShoppingCart::getUserId, userId);
        List<ShoppingCart> shoppingCarts = shoppingCartService.list(lqw);
        if (shoppingCarts == null || shoppingCarts.size() == 0) {
            return R.error("购物车为空，无法下单");
        }
        //查询用户地址信息
        AddressBook addressBook = addressBookService.getById(orders.getAddressBookId());
        if (addressBook == null) {
            return R.error("地址信息有误，无法下单");
        }
        long orderId = IdWorker.getId();

        AtomicInteger amount = new AtomicInteger(0);

        List<OrderDetail> orderDetails = shoppingCarts.stream().map((item) -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setNumber(item.getNumber());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setSetmealId(item.getSetmealId());
            orderDetail.setName(item.getName());
            orderDetail.setImage(item.getImage());
            orderDetail.setAmount(item.getAmount());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());


        orders.setId(orderId);
        orders.setOrderTime(LocalDateTime.now());
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setStatus(2);
        orders.setAmount(new BigDecimal(amount.get()));//总金额
        orders.setUserId(userId);
        orders.setNumber(String.valueOf(orderId));
        orders.setUserName(user.getName());
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        orders.setAddress((addressBook.getProvinceName() == null ? "" : addressBook.getProvinceName())
                + (addressBook.getCityName() == null ? "" : addressBook.getCityName())
                + (addressBook.getDistrictName() == null ? "" : addressBook.getDistrictName())
                + (addressBook.getDetail() == null ? "" : addressBook.getDetail()));

        //向订单表插入数据，一条数据
        ordersService.save(orders);
        //向订单明细表插入多条数据
        orderDetailService.saveBatch(orderDetails);
        //清空购物车
        shoppingCartService.remove(lqw);

        return R.success("下单成功");
    }

}
