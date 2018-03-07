package com.backend.business.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Setter
@Getter
@ConfigurationProperties("sharding")
public class ShardingDataSourceProperties {
    private boolean enabled = true;
    private String ruleConfigLocation;
    private String defaultDataSourceName;
    private Map<String, DataSourceInstance> dataSources = new HashMap<>();

    @Setter
    @Getter
    public static class DataSourceInstance {
        @NestedConfigurationProperty
        private DataSourceProperties master;
        @NestedConfigurationProperty
        private List<DataSourceProperties> slaves;
    }

    @Setter
    @Getter
    public static class DataSourceProperties {
        /**
         * Default Transaction Isolation. See: <p>
         * {@link Connection#TRANSACTION_NONE} <p>
         * {@link Connection#TRANSACTION_READ_UNCOMMITTED} etc
         */
        private Integer defaultTransactionIsolation = Connection.TRANSACTION_READ_COMMITTED;
        private String driverClassName;
        private transient String url;
        private transient String username;
        private transient String password;
        private boolean defaultAutoCommit = true;
        private int initialSize = 5;
        private int maxActive = 30;
        private int minIdle = 8;
        private long maxWaitMillis = 5000;
        private String validationQuery = "SELECT 1";
        private boolean testOnBorrow = false;
        private boolean testOnReturn = false;
        private boolean testWhileIdle = true;
        private long timeBetweenEvictionRunsMillis = 60 * 1000L;
        private long minEvictableIdleTimeMillis = 30 * 60 * 1000L;
        private boolean removeAbandoned = false;
        private long removeAbandonedTimeoutMillis = 5 * 60 * 1000L;
        private boolean logAbandoned = false;
    }

}
