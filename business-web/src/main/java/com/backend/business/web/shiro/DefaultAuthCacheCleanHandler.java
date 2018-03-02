package com.backend.business.web.shiro;

import com.backend.business.web.properties.ShiroProperties;
import org.springframework.data.redis.core.RedisTemplate;

public class DefaultAuthCacheCleanHandler implements AuthCacheCleanHandler {

    private final RedisTemplate<String, Object> sessionRedisTemplate;
    private final ShiroProperties shiroProperties;

    public DefaultAuthCacheCleanHandler(RedisTemplate<String, Object> sessionRedisTemplate, ShiroProperties shiroProperties) {
        this.sessionRedisTemplate = sessionRedisTemplate;
        this.shiroProperties = shiroProperties;
    }

    @Override
    public void cleanAuthenticationCache(String principal) {
        String cachePrefix = shiroProperties.getCache().getAuthenticationCachePrefix();
        sessionRedisTemplate.delete(cachePrefix + principal);
    }

    @Override
    public void cleanAuthorizationCache(String principal) {
        String cachePrefix = shiroProperties.getCache().getAuthorizationCachePrefix();
        sessionRedisTemplate.delete(cachePrefix + principal);
    }

}
