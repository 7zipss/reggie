package com.ps.common;

import lombok.extern.slf4j.Slf4j;

@Slf4j
/**
 * 基于ThreadLocal封装此工具类, 保存和获取此线程用户id
 */
public class BaseContext {

    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    /**
     * 设置当前线程用户id
     * @param id
     */
    public static void setCurrentId(Long id){
        threadLocal.set(id);
    }

    /**
     * 获取当前用户id
     * @return
     */
    public static Long getCurrentId(){
        Long currentId = threadLocal.get();
        log.info(currentId.toString());
        return currentId;
    }

}
