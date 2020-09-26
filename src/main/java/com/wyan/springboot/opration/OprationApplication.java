package com.wyan.springboot.opration;


import com.wyan.springboot.opration.config.annotation.DistributeCache;
import com.wyan.springboot.opration.entity.KeyToken;
import com.wyan.springboot.opration.entity.UserInfo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.io.Serializable;
import java.util.concurrent.TimeUnit;


@RestController
@EnableAspectJAutoProxy
@SpringBootApplication
public class OprationApplication {

    public static void main(String[] args) {
        SpringApplication.run(OprationApplication.class, args);
    }

    @DistributeCache(key="$name",distExpireTime=1000,unit=TimeUnit.MILLISECONDS)
    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return String.format("Hello %s!", name);
    }

    @DistributeCache//(key= KeyToken.AIC_RD_INFO_ID_PG_DT+"#{user.name}:#{user.id}",isLocalCache = true,distExpireTime = 5,localExpireTime = 10,unit = TimeUnit.MINUTES)
    @PostMapping("hello2")
    public String hello2(UserInfo user) {
        return String.format("Hello %s!", user.getName());
    }

//    public OprationApplication(RedisTemplate<String, Serializable> redisTemplate) {
//        redisTemplate.opsForValue().set("hello", "world");
//        String ans = redisTemplate.opsForValue().get("hello").toString();
//        redisTemplate.opsForValue().set("wyan","hahahahahahahaha",5, TimeUnit.MINUTES);
//        String r = redisTemplate.opsForValue().get("wyan").toString();
//        Assert.isTrue("world".equals(ans));
//    }
}
