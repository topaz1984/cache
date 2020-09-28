# **cache分布式缓存+本地二级缓存**   
## **缓存实现原理:**      
      通过AOP+自定义注解+反射+redis+concurrentHashMap来实现，分布式为第一层缓存，本地为二级缓存，原则上本地的时间要长于分布式缓存，一定程度上解决缓存穿透的问题。   
      本地缓存中通过concurrentHashMap本身分段锁和线程安全的特性，再加上LinkedHashMap来进行Lru的缓存清除策略。后续会补充相关定时删除过期数据的方法。   


## 1.使用样例
     @DistributeCache(key= "agent:monitor:test:"+"#{user.name}:#{user.id}",isLocalCache = true,distExpireTime = 5,localExpireTime = 10,unit = TimeUnit.MINUTES)   
     @PostMapping("hello2")   
     public String hello2(UserInfo user) {   
       return String.format("Hello %s!", user.getName());   
     }     
    
    
## 2.说明：   
   注意：必须作用于接口方法，不要加在controller层，因为controller层一般会有自己定义的返回体   
   
   
## 3.定义参数解析：   
   ### 3.1使用格式：   
       >@DistributeCache(key= "agent:monitor:test:"+"#{user.name}:#{user.id}",isLocalCache = true,distExpireTime = 5,localExpireTime = 10,unit = TimeUnit.MINUTES)   
   ### 3.2格式说明:      
       1.格式建议以":"分割，因为适合redis中的结构，且后台程序也是以":"来进行分割     
       2.自定义参数可以是字符串，可以是请求对象中的只域但是要使用"#{xxx.id}"的格式。举例："#{user.name}" 对应UserInfo user中的name属性      
   ### 3.3注解类说明：                
   >@Target(ElementType.METHOD)   
   @Retention(RetentionPolicy.RUNTIME)   
   public @interface DistributeCache {   
      String key() default "";   

      int distExpireTime() default 5;//默认5分钟   

      int localExpireTime() default 10;//本地缓存的默认时间10分钟   

      TimeUnit unit() default TimeUnit.MINUTES;//默认的time类别为分钟   

      boolean isOpen() default true;//默认开启分布式缓存   

      boolean globalIsOpen() default true;//全局缓存配置配置 {对整个服务层接口起作用}   

      /*
       *本地缓存 预防分布式缓存出现异常或者防止穿透，原则上失效时间大于分布式缓存   
      */
      boolean isLocalCache() default false;//是否开启本地缓存   

      String describes() default "";//缓存说明   
   }   
