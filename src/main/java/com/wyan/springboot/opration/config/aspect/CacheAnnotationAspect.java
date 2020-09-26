package com.wyan.springboot.opration.config.aspect;

import com.wyan.springboot.opration.config.annotation.DistributeCache;
import com.wyan.springboot.opration.config.aspect.util.AnnotationParamResolver;
import com.wyan.springboot.opration.config.aspect.util.LocalCacheUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author wyan
 * @title: CacheAnnotationAspect
 * @projectName opration
 * @description: 缓存注解切面类
 * @date 2020/8/25 10:41 上午
 * @company 西南凯亚
 */
@Component
@Aspect
public class CacheAnnotationAspect {

    private static final Logger logger = LoggerFactory.getLogger(CacheAnnotationAspect.class);

    @Resource
    private RedisTemplate redisTemplate;


    /**
     * @param joinPoint
     * @param cache     注解参数body
     * @return
     * @Descriptions
     */
    @Around("@annotation(cache)")
    public Object executeCacheLogic(ProceedingJoinPoint joinPoint, DistributeCache cache) {
        Object key = null;
        AnnotationParamResolver resolverBuss= null;
        Signature signature = joinPoint.getSignature();
        if (!(signature instanceof MethodSignature)) {
            logger.error("DistributeCache注解用法不合法，只能于方法！keyInfo: {}",cache.key());
            throw new IllegalArgumentException("DistributeCache注解用法不合法，只能于方法！");
        }
        //判断key是否为"" || null
        if (null == cache.key() || "".equals(cache.key())) {
            //执行默认key生成 生成规则-> 待补充
            key = gentKey(joinPoint);
        } else {
            resolverBuss = AnnotationParamResolver.newInstance();
            String spellKey = cache.key();
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                key = resolverBuss.preResolver(joinPoint, spellKey);
            }
        }

        //内存有则直接返回，否则执行后台逻辑
        Object result = redisTemplate.opsForValue().get(key);
        if (Objects.nonNull(result)) return result;
        try {
            result = joinPoint.proceed();//执行原接口中的方法
        } catch (Throwable throwable) {
            //System.out.println("分布式缓存方法中原接口方法执行出错！");
            logger.error("分布式缓存方法中原接口方法执行出错！！ProxiedClassInfos: {}",joinPoint.getTarget().getClass().getName());
            throwable.printStackTrace();
            return null;
        }
        String skey = key.toString();
        redisTemplate.opsForValue().set(skey, result, cache.distExpireTime(), cache.unit());
        if(cache.isLocalCache()) {
            LocalCacheUtils localCache = LocalCacheUtils.getInstance();
            localCache.putValue(skey,result,cache.localExpireTime(),cache.unit());
        }
        //System.out.println(redisTemplate.opsForValue().get(key));

        return result;
    }

    /**
     * @param joinPoint
     * @return
     * @descriptions 如果但前注解中没有配置任何参数key的生成有效参数，则执行次方法进行默认的
     * key生成规则;
     * 为空则生成新的key，生成规则[key-> ] 需要考虑参数过多的情况，保证key不要过大
     *  TODO 生成obj对象toString的hash值
     * 否则直接返回
     */
    private static Object gentKey(ProceedingJoinPoint joinPoint) {
        Object value;
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String[] names = methodSignature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        List paramList = new ArrayList<Object>();
        for (int i = 0; i < names.length; i++) paramList.add(names[i] +"="+ Objects.hash(args[i].toString()));
        value = paramList.stream().filter(v -> v != null).map(Objects::toString).collect(Collectors.joining(",","[","]"));
        return value;
    }


}
