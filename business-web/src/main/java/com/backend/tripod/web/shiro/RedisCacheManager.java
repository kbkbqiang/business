package com.backend.tripod.web.shiro;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.util.Destroyable;
import org.springframework.data.redis.core.RedisTemplate;

public class RedisCacheManager implements CacheManager, Destroyable {

    private RedisTemplate<String, Object> redisTemplate;
    private long timeOutHour = 1;


    public RedisCacheManager(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void setTimeOutHour(long timeOutHour) {
        this.timeOutHour = timeOutHour;
    }

    @Override
    public Cache getCache(String name) throws CacheException {
        return new RedisCache(name, redisTemplate, timeOutHour);
    }

    @Override
    public void destroy() throws Exception {

    }
}
