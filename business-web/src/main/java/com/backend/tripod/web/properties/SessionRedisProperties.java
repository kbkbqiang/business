package com.backend.tripod.web.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "session-redis")
@Setter
@Getter
public class SessionRedisProperties {
    private ConnectionStrategy strategy = ConnectionStrategy.SHARE_WITH_BIZ;
    private int database = 0;
    private String url;
    private String host = "localhost";
    private String password;
    private int port = 6379;
    private boolean ssl;
    private int timeout;
    private Pool pool;
    private Sentinel sentinel;
    private Cluster cluster;

    public static class Pool extends RedisProperties.Pool {

    }

    public static class Sentinel extends RedisProperties.Sentinel {

    }

    public static class Cluster extends RedisProperties.Cluster {

    }

    public enum ConnectionStrategy {
        ISOLATION_FROM_BIZ,
        SHARE_WITH_BIZ
    }

}
