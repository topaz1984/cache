package com.wyan.springboot.opration.service.impl;

import com.wyan.springboot.opration.config.annotation.DistributeCache;
import com.wyan.springboot.opration.config.aspect.util.AnnotationParamResolver;
import com.wyan.springboot.opration.config.aspect.util.LocalCacheUtils;
import com.wyan.springboot.opration.service.CacheService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author wyan
 * @title: CacheServiceImpl
 * @projectName opration
 * @description: TODO
 * @date 2020/9/26 10:11 下午
 */
@Service
public class CacheServiceImpl implements CacheService {

    private static final Logger logger = LoggerFactory.getLogger(CacheServiceImpl.class);

    @Resource
    private RedisTemplate redisTemplate;

  @Override
  public Object exeCache(DistributeCache cache, ProceedingJoinPoint joinPoint) {
    Object key = null;
    Object result = null;
    AnnotationParamResolver resolverBuss = null;
    boolean isLocal = cache.isLocalCache();//是否开启本地缓存
    //判断当前key是否有自定义值
    if ("".equals(cache.key())) {
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

    //先从缓存中获取当前key的值
    result = redisTemplate.opsForValue().get(key);
    //内存有则直接返回，否则执行后台逻辑
    if (Objects.nonNull(result)) return result;
    //判断是否配置本地缓存，如果有再执行本地缓存查询
    if (isLocal) {
      result =  LocalCacheUtils.getInstance().getValue(cache.key());
      if (Objects.nonNull(result)) return result;
    }

    try {
      result = joinPoint.proceed();//执行原接口中的方法获取结果
    } catch (Throwable throwable) {
      //System.out.println("分布式缓存方法中原接口方法执行出错！");
      logger.error("分布式缓存方法中原接口方法执行出错！！ProxiedClassInfos: {}", joinPoint.getTarget().getClass().getName());
      throwable.printStackTrace();
      return null;
    }
    //执行分布式缓存存储
    redisTemplate.opsForValue().set(key, result, cache.distExpireTime(), cache.unit());
    if (isLocal) {
      LocalCacheUtils.getInstance().putValue(key.toString(),result,cache.localExpireTime(),cache.unit());
    }
    return result;
  }


    /**
     * 生成一个key，生成方式按照参数体来以":"来进行拼接
     * @param joinPoint
     * @return
     */
    private static String gentKey(ProceedingJoinPoint joinPoint) {
        Object value = null;
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String methodName = methodSignature.getMethod().getName();
        String[] names = methodSignature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        List paramList = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            Object obj = args[i];
//            Method[] methods = obj.getClass().getDeclaredMethods();
//            Field[] fields = obj.getClass().getFields();
            Field[] fields = obj.getClass().getDeclaredFields();
            for(Field field: fields){
                String name = field.getName();
                field.setAccessible(true); // 私有属性必须设置访问权限
                Object resultValue = null;
                try {
                    resultValue = field.get(obj);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                // 补充日志
                System.out.println(name + ": " + resultValue);
                if(Objects.nonNull(resultValue) && !"".equals(resultValue.toString().trim())){
                    paramList.add(resultValue.hashCode());
                }
            }

        }
       return methodName + paramList.stream().filter(v -> v != null).map(Objects::toString).collect(Collectors.joining()).hashCode();
    }
}
