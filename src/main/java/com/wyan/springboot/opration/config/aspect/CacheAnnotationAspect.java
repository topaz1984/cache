package com.wyan.springboot.opration.config.aspect;

import com.wyan.springboot.opration.config.annotation.DistributeCache;
import com.wyan.springboot.opration.config.aspect.util.AnnotationParamResolver;
import com.wyan.springboot.opration.service.CacheService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


/**
 * @author wyan
 * @title: CacheAnnotationAspect
 * @projectName opration
 * @description: 缓存注解切面类
 * @date 2020/8/25 10:41 上午
 */
@Component
@Aspect
public class CacheAnnotationAspect {

    private static final Logger logger = LoggerFactory.getLogger(CacheAnnotationAspect.class);

    @Resource
    CacheService cacheService;

    /**
     * @param joinPoint
     * @param cache     注解参数body
     * @return
     * @Descriptions 此方法中只验证当前注解使用的合法性，只能加载方法上
     */
    @Around("@annotation(cache)")
    public Object executeCacheLogic(ProceedingJoinPoint joinPoint, DistributeCache cache) {
        Object key = null;
        AnnotationParamResolver resolverBuss = null;
        Signature signature = joinPoint.getSignature();
        if (!(signature instanceof MethodSignature)) {
            logger.error("DistributeCache注解用法不合法，只能于方法！keyInfo: {}", cache.key());
            throw new IllegalArgumentException("DistributeCache注解用法不合法，只能于方法！");
        }
        //执行缓存接口
        return cacheService.exeCache(cache, joinPoint);
    }


}
