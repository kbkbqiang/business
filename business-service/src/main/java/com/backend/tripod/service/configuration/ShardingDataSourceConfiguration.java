package com.backend.tripod.service.configuration;


import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.backend.tripod.service.properties.ShardingDataSourceProperties;
import com.dangdang.ddframe.rdb.sharding.api.HintManager;
import com.dangdang.ddframe.rdb.sharding.api.MasterSlaveDataSourceFactory;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.config.common.api.ShardingRuleBuilder;
import com.dangdang.ddframe.rdb.sharding.config.yaml.internel.YamlConfig;
import com.dangdang.ddframe.rdb.sharding.hint.HintManagerHolder;
import com.dangdang.ddframe.rdb.sharding.jdbc.core.datasource.ShardingDataSource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.Assert;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(ShardingDataSourceProperties.class)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@ConditionalOnProperty(prefix = "sharding", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableTransactionManagement
public class ShardingDataSourceConfiguration {
    private static final Logger logger = LoggerFactory.getLogger(ShardingDataSourceConfiguration.class);

    private final ShardingDataSourceProperties shardingDataSourceProperties;
    private ResourceLoader resourceLoader = new DefaultResourceLoader();

    public ShardingDataSourceConfiguration(ShardingDataSourceProperties shardingDataSourceProperties) {
        this.shardingDataSourceProperties = shardingDataSourceProperties;
    }

    /**
     * 创建sharding datasource. <p>
     * 该datasource里面包含了各个分组的datasource
     *
     * @return
     * @throws Exception
     */
    @Bean
    @ConditionalOnMissingBean
    @Primary
    public DataSource dataSource() throws Exception {
        String defaultDataSourceName = shardingDataSourceProperties.getDefaultDataSourceName();
        Assert.isTrue(StringUtils.isNotBlank(defaultDataSourceName), "default-data-source-name for sharding must be specified");
        String ruleConfigLocation = shardingDataSourceProperties.getRuleConfigLocation();
        Map<String, DataSource> dataSources = new HashMap<>();
        Map<String, ShardingDataSourceProperties.DataSourceInstance> dataSourceMap = shardingDataSourceProperties.getDataSources();
        boolean matchDefaultDataSource = false;
        for (Map.Entry<String, ShardingDataSourceProperties.DataSourceInstance> dsGroup : dataSourceMap.entrySet()) {
            String groupName = dsGroup.getKey();
            if (!matchDefaultDataSource && defaultDataSourceName.equals(groupName)) {
                matchDefaultDataSource = true;
            }
            ShardingDataSourceProperties.DataSourceInstance groupConfig = dsGroup.getValue();
            ShardingDataSourceProperties.DataSourceProperties masterConf = groupConfig.getMaster();
            DataSource masterDataSource = actualDataSource(masterConf, true);
            logger.info("created master datasource {} with config: {}", groupName, JSON.toJSONString(masterConf, SerializerFeature.UseSingleQuotes));

            List<ShardingDataSourceProperties.DataSourceProperties> slaves = groupConfig.getSlaves();
            if (null == slaves || slaves.isEmpty()) {
                dataSources.put(groupName, masterOnlyGroupDataSource(groupName, masterDataSource));
                continue;
            }
            Map<String, DataSource> slaveDataSourceMap = new LinkedHashMap<>();
            for (int i = 0; i < slaves.size(); i++) {
                String slaveName = groupName + "-slave-" + i;
                ShardingDataSourceProperties.DataSourceProperties slaveConf = slaves.get(i);
                DataSource slaveDataSource = actualDataSource(slaveConf, false);
                logger.info("created slave  datasource {} with config: {}", slaveName, JSON.toJSONString(slaveConf, SerializerFeature.UseSingleQuotes));
                slaveDataSourceMap.put(slaveName, slaveDataSource);
            }
            dataSources.put(groupName, masterSlavesGroupDataSource(groupName, masterDataSource, slaveDataSourceMap));
        }
        Assert.isTrue(matchDefaultDataSource, "no datasource matched for default-data-source-name:" + defaultDataSourceName);
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        YamlConfig yamlConfig;
        ShardingRule shardingRule;
        try {
            Resource resource = resourceLoader.getResource(ruleConfigLocation);
            inputStream = resource.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            yamlConfig = new Yaml(new Constructor(YamlConfig.class)).loadAs(inputStreamReader, YamlConfig.class);
            yamlConfig.setDefaultDataSourceName(defaultDataSourceName);
            shardingRule = new ShardingRuleBuilder(ruleConfigLocation, dataSources, yamlConfig).build();
            return new ShardingDataSource(shardingRule, yamlConfig.getProps());
        } finally {
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    protected DataSource masterOnlyGroupDataSource(String groupName, DataSource masterDataSource) {
        return masterDataSource;
    }

    /**
     * 创建分组datasource.<p>
     * 组内包含具体的datasource(各个master & slaves)
     *
     * @param groupName          组名
     * @param masterDataSource   该组主数据源
     * @param slaveDataSourceMap 该组从数据源
     * @return
     * @throws SQLException
     */
    protected DataSource masterSlavesGroupDataSource(String groupName, DataSource masterDataSource, Map<String, DataSource> slaveDataSourceMap) throws SQLException {
        return MasterSlaveDataSourceFactory.createDataSource(groupName, groupName + "-master", masterDataSource, slaveDataSourceMap);
    }

    /**
     * 创建分组下具体的datasource
     *
     * @param config   数据源连接配置
     * @param isMaster 是否是主库
     * @return
     */
    protected DataSource actualDataSource(ShardingDataSourceProperties.DataSourceProperties config, boolean isMaster) {
        DruidDataSource ds = new DruidDataSource();
        ds.setDriverClassName(config.getDriverClassName());
        ds.setUrl(config.getUrl());
        ds.setUsername(config.getUsername());
        ds.setPassword(config.getPassword());
        if (config.getDefaultTransactionIsolation() != null) {
            ds.setDefaultTransactionIsolation(config.getDefaultTransactionIsolation());
        }
        if (StringUtils.isBlank(config.getValidationQuery())) {
            ds.setValidationQuery(config.getValidationQuery());
        }
        ds.setDefaultAutoCommit(config.isDefaultAutoCommit());
        ds.setInitialSize(config.getInitialSize());
        ds.setMaxActive(config.getMaxActive());
        ds.setMinIdle(config.getMinIdle());
        ds.setMaxWait(config.getMaxWaitMillis());
        ds.setTestOnBorrow(config.isTestOnBorrow());
        ds.setTestOnReturn(config.isTestOnReturn());
        ds.setTestWhileIdle(config.isTestWhileIdle());
        ds.setTimeBetweenEvictionRunsMillis(config.getTimeBetweenEvictionRunsMillis());
        ds.setMinEvictableIdleTimeMillis(config.getMinEvictableIdleTimeMillis());
        ds.setRemoveAbandoned(config.isRemoveAbandoned());
        ds.setRemoveAbandonedTimeoutMillis(config.getRemoveAbandonedTimeoutMillis());
        ds.setLogAbandoned(config.isLogAbandoned());
        return ds;
    }

    @Bean
    @ConditionalOnMissingBean(PlatformTransactionManager.class)
    public DataSourceTransactionManager transactionManager(DataSource dataSource) {
        return new ShardingDataSourceTransactionManager(dataSource);
    }

    public static class ShardingDataSourceTransactionManager extends DataSourceTransactionManager {
        private static final long serialVersionUID = -1459584315029874109L;

        ShardingDataSourceTransactionManager(DataSource dataSource) {
            super(dataSource);
        }

        @Override
        protected void doBegin(Object transaction, TransactionDefinition definition) {
            if (!HintManagerHolder.isMasterRouteOnly()) {
                HintManager hintManager = HintManager.getInstance();
                hintManager.setMasterRouteOnly();
            }
            super.doBegin(transaction, definition);
        }

        @Override
        protected void doResume(Object transaction, Object suspendedResources) {
            if (!HintManagerHolder.isMasterRouteOnly()) {
                HintManager hintManager = HintManager.getInstance();
                hintManager.setMasterRouteOnly();
            }
            super.doResume(transaction, suspendedResources);
        }

        @Override
        protected Object doSuspend(Object transaction) {
            HintManagerHolder.clear();
            return super.doSuspend(transaction);
        }

        @Override
        protected void doCleanupAfterCompletion(Object transaction) {
            HintManagerHolder.clear();
            super.doCleanupAfterCompletion(transaction);
        }
    }

}
