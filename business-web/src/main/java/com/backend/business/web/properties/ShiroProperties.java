package com.backend.business.web.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties("shiro")
@Getter
@Setter
public class ShiroProperties {
    private boolean enabled = true;
    private boolean renderPageMode = false;
    @NestedConfigurationProperty
    private SessionProperties session = new SessionProperties();
    @NestedConfigurationProperty
    private HashedCredentialProperties hashedCredential = new HashedCredentialProperties();
    @NestedConfigurationProperty
    private CacheProperties cache = new CacheProperties();

    @Getter
    @Setter
    public static class SessionProperties {
        private long timeOutHours = 2;
        private String prefix = "";
        private SessionTTLPolicy ttlPolicy = SessionTTLPolicy.RESET;
    }

    @Setter
    @Getter
    public static class CacheProperties {
        private boolean enabled = true;
        private boolean authenticationCachingEnabled = false;
        private boolean authorizationCachingEnabled = false;
        private String authenticationCachePrefix = "";
        private String authorizationCachePrefix = "";
        private long timeOutHours = 2;
    }

    @Setter
    @Getter
    public static class HashedCredentialProperties {
        private String hashAlgorithName = "md5";
        private int hashIterations = 3;
    }

    public enum SessionTTLPolicy {
        /**
         * 重置TTL，重新计算session过期时间
         */
        RESET,
        /**
         * 不重置TTL
         */
        ELAPSE;
    }
}
