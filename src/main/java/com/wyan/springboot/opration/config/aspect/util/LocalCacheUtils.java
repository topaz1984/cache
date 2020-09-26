package com.wyan.springboot.opration.config.aspect.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author wyan
 * @title: LocalCacheUtils 本地缓存工具类
 * @projectName opration
 * @description: 本地缓存
 * @date 2020/9/2 15:32
 * @company 西南凯亚
 */
public  class  LocalCacheUtils {

    private static final Logger logger = LoggerFactory.getLogger(LocalCacheUtils.class);
    /**
     * 默认本地缓存个数
     */
    private static final Integer DEFAULT_MAX_VOLUME = 500;
    /**
     * 默认本地缓存失效时间  10分钟
     */
    static final long LOCAL_EXPIRE_TIME = 10 * 60 * 1000L;
    /**
     * 缓存对象
     */
    private static final Map<String, CacheEntry> LOCAL_CACHE_MAP = new ConcurrentHashMap(DEFAULT_MAX_VOLUME);

    /**
     * 记录当前缓存数量 与LRU配合使用
     */
    private static final AtomicInteger CURRENT_CACHE_NUMBERS = new AtomicInteger();

    /**
     * LRU
     * 集合用于在满载时清理不长用的key-值
     */
    private static final List<String> LRU_LISTS = new LinkedList();

    private static  LocalCacheUtils localCache = null;

    public static LocalCacheUtils getInstance(){
        if(Objects.isNull(localCache)){
            synchronized(LocalCacheUtils.class){
                if(Objects.isNull(localCache)){
                    localCache = new LocalCacheUtils();
                }
            }
        }
        return localCache;
    }

    /**
     * 功能：写入本地缓存
     * 步骤：1-存入前判断当前容器是否超过预设量 2-判断时间，没有定义则使用自定义值 3-存储 4.当前缓存数量+1
     *
     * @param key        键
     * @param value      值
     * @param expireTime 过期时间
     */
    public synchronized boolean putValue(String key, Object value, long expireTime, TimeUnit timeUnit) {
        checkBoxCapacityCache();

        if (expireTime <= 0) {
            expireTime = LOCAL_EXPIRE_TIME;
        }else{
            switch(timeUnit){
                case SECONDS:
                    expireTime = expireTime * 1000;
                    break;
                case MINUTES:
                    expireTime = expireTime * 1000 * 60;
                    break;
                case HOURS:
                    expireTime = expireTime * 60 * 60 * 1000;
                    break;
                case DAYS:
                    expireTime = expireTime * 24 * 60 * 60 * 1000;
                    break;
                default:
                    break;
            }
        }

        expireTime = System.currentTimeMillis() + expireTime;
        CacheEntry entry = new CacheEntry(expireTime, value);
        LOCAL_CACHE_MAP.put(key, entry);//放入缓存
        LRU_LISTS.add(key);//放入LRU队列中
        CURRENT_CACHE_NUMBERS.addAndGet(1);//当前缓存数量+1
        return true;
    }

    /**
     * 功能：设置LRU队列
     * 主要是为了解决缓存中如果没有过期值可以删除时，
     * 根据LRU特点来进行最少使用删除
     *
     * @param key
     */
    private static void putLRU(String key) {
        synchronized (LRU_LISTS) {
            LRU_LISTS.remove(key);
            LRU_LISTS.add(0, key);
        }
    }

    /**
     * 根据当前key来获取缓存对象
     *
     * @param key
     */
    public static Object getValue(String key) {
        CacheEntry entity = LOCAL_CACHE_MAP.get(key);
        return entity.getValue();
    }

    /**
     * 清理过期缓存
     *
     * @Todo 1.判断当前key是/否 在过期时间内来进行删或保留 2.判断当前空间是否满载 若满载执行LRU
     */
    private static void clearExpireTimeCache() {
        for (Map.Entry<String, CacheEntry> entry : LOCAL_CACHE_MAP.entrySet()) {
            long expireTime = entry.getValue().getExpireTime();
            if (expireTime < System.currentTimeMillis()) {
                deleteKeyFromLocalCache(entry.getKey());
            }
        }
    }

    /**
     * 避免缓存出现数据不一致 或 线上数据库直接修数后的手动执行
     * 正常生产中不会主动执行
     */
    public static void clearAllCache() {
        LOCAL_CACHE_MAP.clear();
        LRU_LISTS.clear();
    }

    /**
     * 释放本地缓存空间
     */
    private static void checkBoxCapacityCache() {
        //过期数据检查
        clearExpireTimeCache();
        //执行LRU 如果上一步骤可能没有任何过期项被释放就强制清楚最近少用的数据项
        delLRU();
    }

    /**
     * 删除最近少用的缓存key
     */
    private static void delLRU() {
        synchronized (LRU_LISTS) {
            if (LRU_LISTS.size() >= DEFAULT_MAX_VOLUME - 10) {
                String key = null;
                key = LRU_LISTS.remove(LRU_LISTS.size() - 1);
                if (null != key) {
                    deleteKeyFromLocalCache(key);
                }
                //logger.debug("执行LRU替换较少使用key,当前LRU集合：%s ",LRU_LISTS.toString());
                System.out.println("执行LRU替换较少使用key,当前LRU集合：" + LRU_LISTS.toString());
            }
        }
    }

    /**
     * 从缓存中删除当前key
     *
     * @param delKey
     */
    private static void deleteKeyFromLocalCache(String delKey) {
        Object key = LOCAL_CACHE_MAP.remove(delKey);
        if (null != key) {
            int num = CURRENT_CACHE_NUMBERS.incrementAndGet();
            //logger.debug("当前缓存中删除的key: %s 当前缓存数量：",key,num);
            System.out.println("当前缓存中删除的key：" + key + "当前缓存数量：" + num);
        }
    }

}
