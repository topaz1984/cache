# **cache分布式缓存+本地二级缓存**   

## **缓存实现原理:**      
      通过AOP+自定义注解+反射+redis+concurrentHashMap来实现，分布式为第一层缓存，本地为二级缓存，原则上本地的时间要长于分布式缓存，一定程度上解决缓存穿透的问题。   
      本地缓存中通过concurrentHashMap本身分段锁和线程安全的特性，再加上LinkedList来进行LRU的缓存清除策略。后续会补充相关定时删除过期数据的方法。   
![图1 缓存执行流程](https://github.com/topaz1984/cache/blob/master/%E7%BC%93%E5%AD%98%E6%B5%81%E7%A8%8B.png ''执行流程'')
      
      
## 0.针对场景   
     1.读多写少，且密集型访问的场景或这每次需要聚合一定数据但是更新不频繁的集合数据。   
     2.分布式缓存主要应对集群数一致性的访问，本地缓存防止一定穿透。   
     3.设计初衷是为了解决部分简单查询返回集合的场景中，每个方法都要使用redis模板类来做相似的代码，且定义的key命名五花八门不统一。   
     
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
       @DistributeCache(key= "agent:monitor:test:"+"#{user.name}:#{user.id}",isLocalCache = true,distExpireTime = 5,localExpireTime = 10,unit = TimeUnit.MINUTES)   
       
   ### 3.2格式说明:      
       1.格式建议以":"分割，因为适合redis中的结构，且后台程序也是以":"来进行分割        
       2.自定义参数可以是字符串，可以是请求对象中的只域但是要使用"#{xxx.id}"的格式。举例："#{user.name}" 对应UserInfo user中的name属性   
       
   ### 3.3注解类说明：                   
         @Target(ElementType.METHOD)   
         @Retention(RetentionPolicy.RUNTIME)   
         public @interface DistributeCache {   
            String key() default "";   

            int distExpireTime() default 5;//默认5分钟   

            int localExpireTime() default 10;//本地缓存的默认时间10分钟   

            TimeUnit unit() default TimeUnit.MINUTES;//默认的time类别为分钟   

            boolean isOpen() default true;//默认开启分布式缓存   

   ~~boolean globalIsOpen() default true;//全局缓存配置配置 {对整个服务层接口起作用}~~   后续补充全局控制功能

            /*
             *本地缓存 预防分布式缓存出现异常或者防止穿透，原则上失效时间大于分布式缓存   
            */
            boolean isLocalCache() default false;//是否开启本地缓存   

            String describes() default "";//缓存说明   
         }   
