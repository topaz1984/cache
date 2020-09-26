package com.wyan.springboot.opration.config.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * @author wyan
 * @title: com.wyan.springboot.opration.config.annotation.DistributeCache
 * @projectName opration
 * @description: 定义分布式注解，用来放在接口方法上
 * @date 2020/8/2510:18 上午
 * @company 西南凯亚
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributeCache {
    String key() default "";

    int distExpireTime() default 5;//默认5分钟

    int localExpireTime() default 10;

    TimeUnit unit() default TimeUnit.MINUTES;

    boolean isOpen() default true;//默认开启分布式缓存

    boolean globalIsOpen() default true;//全局缓存配置配置 {对整个服务层接口起作用}

    /*
     *本地缓存 预防分布式缓存出现异常或者防止穿透，原则上失效时间大于分布式缓存
     */
    boolean isLocalCache() default true;//是否开启本地缓存

    String describes() default "";//缓存说明
}
