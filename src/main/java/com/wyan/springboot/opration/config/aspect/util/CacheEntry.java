package com.wyan.springboot.opration.config.aspect.util;

import java.io.Serializable;

/**
 * @author wyan
 * @title: CacheEntity
 * @projectName aic-cores
 * @description: TODO 本地缓存实体类
 * @date 2020/9/19 11:57 上午
 */
public class CacheEntry implements Serializable {

  long expireTime;
  Object value;

  public CacheEntry(){}

  public CacheEntry(long expireTime, Object value) {
    this.expireTime = expireTime;
    this.value = value;
  }

  public void setExpireTime(long expireTime) {
    this.expireTime = expireTime;
  }

  public void setValue(Object value) {
    this.value = value;
  }

  public long getExpireTime() {
    return expireTime;
  }

  public Object getValue() {
    return value;
  }

}
