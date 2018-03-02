package com.backend.business.web.shiro;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RedisCache implements Cache<String, Object> {

    private String prefix;
    private RedisTemplate<String, Object> redisTemplate;
    private long timeOutHour = 1;

    public RedisCache(String prefix, RedisTemplate<String, Object> redisTemplate) {
        this.prefix = prefix;
        this.redisTemplate = redisTemplate;
    }

    public RedisCache(String prefix, RedisTemplate<String, Object> redisTemplate, long timeOutHour) {
        this.prefix = prefix;
        this.redisTemplate = redisTemplate;
        this.timeOutHour = timeOutHour;
    }

    public void setTimeOutHour(long timeOutHour) {
        this.timeOutHour = timeOutHour;
    }

    @Override
    public Object get(String key) throws CacheException {
        if (key == null) {
            return null;
        }
        return redisTemplate.opsForValue().get(prefix + key);
    }

    @Override
    public Object put(String key, Object value) throws CacheException {
        Object previous = get(key);
        redisTemplate.opsForValue().set(prefix + key, value, timeOutHour, TimeUnit.HOURS);
        return previous;
    }

    @Override
    public Object remove(String key) throws CacheException {
        Object previous = get(key);
        redisTemplate.delete(prefix + key);
        return previous;
    }

    @Override
    public void clear() throws CacheException {
        redisTemplate.delete(keys());
    }

    @Override
    public int size() {
        return keys().size();
    }

    @Override
    public Set<String> keys() {
        return redisTemplate.keys(prefix);
    }

    @Override
    public Collection<Object> values() {
        return redisTemplate.opsForValue().multiGet(keys());
    }
}
