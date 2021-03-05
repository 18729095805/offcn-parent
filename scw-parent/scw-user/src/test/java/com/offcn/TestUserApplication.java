package com.offcn;

import com.offcn.user.UserStartApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = UserStartApplication.class ) //启动类启动的时候就启动test
public class TestUserApplication {
    @Autowired
    private RedisTemplate redisTemplate;

    //另一种redis
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void TestRedis(){
        redisTemplate.boundValueOps("name").set("506");
        Object name = redisTemplate.boundValueOps("name").get();
        //另一种 数据类型为string
        stringRedisTemplate.opsForValue().set("name2","lucy");
        String name2 = stringRedisTemplate.opsForValue().get("name2");

        System.out.println("name:"+name);
        System.out.println("name2:"+name2);
    }

}
