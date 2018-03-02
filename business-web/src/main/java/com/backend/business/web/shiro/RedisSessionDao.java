package com.backend.business.web.shiro;

import com.backend.business.web.properties.ShiroProperties;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RedisSessionDao extends AbstractSessionDAO {

    private RedisTemplate<String, Object> redisTemplate;
    private final String prefix;
    private final long timeOutHours;
    private final ShiroProperties.SessionTTLPolicy ttlPolicy;

    public RedisSessionDao(RedisTemplate<String, Object> redisTemplate, ShiroProperties.SessionProperties properties) {
        this.redisTemplate = redisTemplate;
        this.prefix = properties.getPrefix();
        this.timeOutHours = properties.getTimeOutHours();
        this.ttlPolicy = properties.getTtlPolicy();
    }

    @Override
    protected Serializable doCreate(Session session) {
        Serializable sessionId = generateSessionId(session);
        String finalSessionId = prefix + sessionId.toString();
        assignSessionId(session, finalSessionId);
        redisTemplate.opsForValue().set(finalSessionId, session, timeOutHours, TimeUnit.HOURS);
        return finalSessionId;
    }

    @Override
    protected Session doReadSession(Serializable sessionId) {
        String id = sessionId.toString();
        if (id.startsWith(prefix)) {
            Object obj = redisTemplate.opsForValue().get(id);
            return obj instanceof Session ? (Session) obj : null;
        }
        return null;
    }

    @Override
    public void update(Session session) throws UnknownSessionException {
        String sessionId = session.getId().toString();
        switch (ttlPolicy) {
            case RESET:
                redisTemplate.opsForValue().set(sessionId, session, timeOutHours, TimeUnit.HOURS);
                break;
            case ELAPSE:
                Long ttl = redisTemplate.getExpire(sessionId);
                //-2 if the key does not exist
                //-1 if the key exists but has no associated expire
                if (ttl < 1) {
                    ttl = timeOutHours * 60 * 60;
                }
                redisTemplate.opsForValue().set(sessionId, session, ttl, TimeUnit.SECONDS);
                break;
            default:
                break;
        }
    }

    @Override
    public void delete(Session session) {
        redisTemplate.delete(session.getId().toString());
    }

    @Override
    public Collection<Session> getActiveSessions() {
        Set<String> keys = redisTemplate.keys(prefix);
        return redisTemplate.opsForValue().multiGet(keys).stream()
                .filter(obj -> obj instanceof Session)
                .map(Session.class::cast)
                .collect(Collectors.toSet());
    }
}
