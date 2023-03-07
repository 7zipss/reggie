package com.ps.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.ps.common.BaseContext;
import com.ps.common.R;
import com.ps.entity.AddressBook;
import com.ps.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.jni.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.annotation.ApplicationScope;

import java.util.List;

@RestController
@RequestMapping("/addressBook")
@Slf4j
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    @GetMapping("/default")
    public R<AddressBook> getDefault(){
        //针对于当前用户
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<AddressBook> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AddressBook::getUserId, currentId)
                .eq(AddressBook::getIsDefault, 1);
        AddressBook addressBook = addressBookService.getOne(lqw);
        return R.success(addressBook);
    }

    @DeleteMapping
    public R<String> delete(Long ids){
        addressBookService.removeById(ids);
        return R.success("删除地址成功");
    }

    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook){
        addressBookService.updateById(addressBook);
        return R.success("修改地址信息成功");
    }

    @GetMapping("/{id}")
    public R<AddressBook> getOne(@PathVariable Long id){
        LambdaQueryWrapper<AddressBook> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AddressBook::getId, id);
        AddressBook addressBook = addressBookService.getOne(lqw);
        return R.success(addressBook);
    }

    @PutMapping("/default")
    public R<String> setDefault(@RequestBody AddressBook addressBook){
        LambdaUpdateWrapper<AddressBook> luw = new LambdaUpdateWrapper<>();
        luw.eq(AddressBook::getUserId, BaseContext.getCurrentId())
                .set(AddressBook::getIsDefault, 0);
        addressBookService.update(luw);
        luw.clear();
        luw.eq(AddressBook::getId, addressBook.getId())
                .set(AddressBook::getIsDefault, 1);
        addressBookService.update(luw);
        return R.success("成功设置为默认地址");
    }

    @PostMapping
    public R<String> save(@RequestBody AddressBook addressBook){
        Long currentId = BaseContext.getCurrentId();
        addressBook.setUserId(currentId);
        addressBookService.save(addressBook);
        return R.success("地址保存成功");
    }

    @GetMapping("/list")
    public R<List<AddressBook>> list(){
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<AddressBook> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AddressBook::getUserId, currentId)
                .orderByDesc(AddressBook::getUpdateTime);
        List<AddressBook> list = addressBookService.list(lqw);
        return R.success(list);
    }

}
