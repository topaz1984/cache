package com.wyan.springboot.opration.service;

import com.wyan.springboot.opration.config.annotation.DistributeCache;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * @author wyan
 * @title: CacheService
 * @projectName opration
 * @description: TODO
 * @date 2020/9/26 10:03 下午
 */
public interface CacheService {
    public Object exeCache(DistributeCache cache, ProceedingJoinPoint joinPoint);
}
