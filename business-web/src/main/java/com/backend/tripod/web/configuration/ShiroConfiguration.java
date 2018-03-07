package com.backend.tripod.web.configuration;

import com.backend.tripod.web.properties.SessionRedisProperties;
import com.backend.tripod.web.properties.ShiroProperties;
import com.backend.tripod.web.shiro.AuthCacheCleanHandler;
import com.backend.tripod.web.shiro.CustomMobileAuthenticationFilter;
import com.backend.tripod.web.shiro.CustomWebSessionManager;
import com.backend.tripod.web.shiro.DefaultAuthCacheCleanHandler;
import com.backend.tripod.web.shiro.KickOffRememberMeManager;
import com.backend.tripod.web.shiro.ModularRealmAuthenticatorEx;
import com.backend.tripod.web.shiro.RedisCacheManager;
import com.backend.tripod.web.shiro.RedisSessionDao;
import com.backend.tripod.web.shiro.ThirdOpenIdRealm;
import com.backend.tripod.web.shiro.UsernamePasswordDeviceRealm;
import com.google.common.collect.Lists;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.mgt.AuthorizingSecurityManager;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.DelegatingFilterProxy;
import redis.clients.jedis.JedisPoolConfig;

import javax.servlet.Filter;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties({ShiroProperties.class, SessionRedisProperties.class})
@ConditionalOnProperty(prefix = "shiro", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ShiroConfiguration implements ApplicationContextAware {

    @Autowired
    private ShiroProperties shiroProperties;
    @Autowired
    private SessionRedisProperties sessionRedis;
    private ApplicationContext applicationContext;
    private final JedisConnectionFactory redisConnectionFactory;
    private final RedisTemplate<String, Object> redisTemplate;
    private List<String> excludeAuthPaths = Lists.newArrayList(
            "/swagger-resources/**",
            "/v2/api-docs",
            "/webjars/**",
            "/static/**",
            "/public/**",
            "/templates/**"
    );

    public ShiroConfiguration(JedisConnectionFactory redisConnectionFactory, @Qualifier("redisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.redisConnectionFactory = redisConnectionFactory;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Bean
    @ConditionalOnMissingBean(AuthorizationAttributeSourceAdvisor.class)
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor() {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager());
        return advisor;
    }

    @Bean
    @ConditionalOnMissingBean(FilterRegistrationBean.class)
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistration = new FilterRegistrationBean();
        filterRegistration.setFilter(new DelegatingFilterProxy("shiroFilter"));
        filterRegistration.addInitParameter("targetFilterLifecycle", "true");//该值缺省为false,表示生命周期由SpringApplicationContext管理,设置为true则表示由ServletContainer管理
        filterRegistration.addInitParameter("targetBeanName", "shiroFilter");
        filterRegistration.setEnabled(true);
        filterRegistration.addUrlPatterns("/*");
        return filterRegistration;
    }

    @Bean("shiroFilter")
    @ConditionalOnMissingBean(ShiroFilterFactoryBean.class)
    public ShiroFilterFactoryBean shiroFilterFactoryBean() {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        shiroFilterFactoryBean.setSecurityManager(securityManager());
        HashMap<String, Filter> filterMap = new LinkedHashMap<>();
        filterMap.put("authc", authenticatingFilter());
        shiroFilterFactoryBean.setFilters(filterMap);
        shiroFilterFactoryBean.setFilterChainDefinitionMap(loadFilterChainDefinitionMap());
        return shiroFilterFactoryBean;
    }

    @Bean
    @ConditionalOnMissingBean(AuthenticatingFilter.class)
    public AuthenticatingFilter authenticatingFilter() {
        CustomMobileAuthenticationFilter authenticationFilter = new CustomMobileAuthenticationFilter();
        authenticationFilter.setRenderPageMode(shiroProperties.isRenderPageMode());
        authenticationFilter.setExcludeAuthPaths(excludeAuthPaths);
        return authenticationFilter;
    }

    private Map<String, String> loadFilterChainDefinitionMap() {
        HashMap<String, String> map = new LinkedHashMap<>();
        excludeAuthPaths.forEach(excludeAuthPath -> map.put(excludeAuthPath, "anon"));
        map.put("/**", "authc");
        return map;
    }

    @Bean
    @ConditionalOnMissingBean(CredentialsMatcher.class)
    public HashedCredentialsMatcher hashedCredentialsMatcher() {
        HashedCredentialsMatcher credentialsMatcher = new HashedCredentialsMatcher();
        credentialsMatcher.setHashAlgorithmName(shiroProperties.getHashedCredential().getHashAlgorithName());
        credentialsMatcher.setHashIterations(shiroProperties.getHashedCredential().getHashIterations());
        return credentialsMatcher;
    }

    @Bean
    public AuthorizingRealm authorizingRealm() {
        UsernamePasswordDeviceRealm realm = new UsernamePasswordDeviceRealm(shiroProperties);
        realm.setCredentialsMatcher(hashedCredentialsMatcher());
        return realm;
    }

    @Bean
    public AuthorizingRealm openIdRealm() {
        return new ThirdOpenIdRealm(shiroProperties);
    }

    @Bean
    @ConditionalOnMissingBean(AuthorizingSecurityManager.class)
    public DefaultWebSecurityManager securityManager() {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setAuthenticator(new ModularRealmAuthenticatorEx());
        securityManager.setRealms(applicationContext.getBeansOfType(Realm.class).values());
        securityManager.setCacheManager(cacheManager());
        SessionManager sessionManager = sessionManager();
        securityManager.setSessionManager(sessionManager);
        securityManager.setRememberMeManager(rememberMeManager());
        return securityManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public RememberMeManager rememberMeManager() {
        return new KickOffRememberMeManager(shiroProperties.getSession().getPrefix(), sessionDao(), shiroRedisTemplate());
    }

    @Bean
    public StringRedisTemplate shiroRedisTemplate() {
        JedisConnectionFactory connectionFactory = sessionRedisConnectionFactory();
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        return template;
    }

    @Bean
    @ConditionalOnMissingBean(DefaultSessionManager.class)
    public SessionManager sessionManager() {
        CustomWebSessionManager sessionManager = new CustomWebSessionManager();
        sessionManager.setDeleteInvalidSessions(true);
        sessionManager.setSessionIdCookieEnabled(false);
        sessionManager.setSessionIdUrlRewritingEnabled(false);
        sessionManager.setGlobalSessionTimeout(Duration.ofHours(shiroProperties.getSession().getTimeOutHours()).getSeconds() * 1000);
        sessionManager.setSessionDAO(sessionDao());
        sessionManager.setSessionListeners(lookupSessionListeners());
        return sessionManager;
    }

    private Collection<SessionListener> lookupSessionListeners() {
        Map<String, SessionListener> beansOfType = applicationContext.getBeansOfType(SessionListener.class);
        return beansOfType.values();
    }

    @Bean
    @ConditionalOnMissingBean(SessionDAO.class)
    public SessionDAO sessionDao() {
        ShiroProperties.SessionProperties sessionProperties = shiroProperties.getSession();
        return new RedisSessionDao(sessionRedisTemplate(), sessionProperties);
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager cacheManager() {
        RedisCacheManager cacheManager = new RedisCacheManager(sessionRedisTemplate());
        cacheManager.setTimeOutHour(shiroProperties.getCache().getTimeOutHours());
        return cacheManager;
    }

    @Bean
    @ConditionalOnMissingBean
    public AuthCacheCleanHandler authCacheCleanHandler() {
        return new DefaultAuthCacheCleanHandler(sessionRedisTemplate(), shiroProperties);
    }

    @Bean
    public RedisTemplate<String, Object> sessionRedisTemplate() {
        if (sessionRedis.getStrategy() == SessionRedisProperties.ConnectionStrategy.SHARE_WITH_BIZ) {
            return redisTemplate;
        }
        if (sessionRedis.getStrategy() == SessionRedisProperties.ConnectionStrategy.ISOLATION_FROM_BIZ) {
            return createSessionRedisTemplate();
        }
        throw new UnsupportedOperationException("unknown session redis connection strategy:" + sessionRedis.getStrategy());
    }

    @Bean
    public JedisConnectionFactory sessionRedisConnectionFactory() {
        if (sessionRedis.getStrategy() == SessionRedisProperties.ConnectionStrategy.SHARE_WITH_BIZ) {
            return redisConnectionFactory;
        }
        if (sessionRedis.getStrategy() == SessionRedisProperties.ConnectionStrategy.ISOLATION_FROM_BIZ) {
            return applyProperties(createJedisConnectionFactory());
        }
        throw new UnsupportedOperationException("unknown session redis connection strategy:" + sessionRedis.getStrategy());
    }

    ////================================================
    ////    session redis connection configuration
    ////================================================

    private RedisTemplate<String, Object> createSessionRedisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JdkSerializationRedisSerializer());
        template.setConnectionFactory(sessionRedisConnectionFactory());
        return template;
    }

    private JedisConnectionFactory applyProperties(JedisConnectionFactory factory) {
        configureConnection(factory);
        if (this.sessionRedis.isSsl()) {
            factory.setUseSsl(true);
        }
        factory.setDatabase(this.sessionRedis.getDatabase());
        if (this.sessionRedis.getTimeout() > 0) {
            factory.setTimeout(this.sessionRedis.getTimeout());
        }
        return factory;
    }

    private void configureConnection(JedisConnectionFactory factory) {
        if (StringUtils.hasText(this.sessionRedis.getUrl())) {
            configureConnectionFromUrl(factory);
        } else {
            factory.setHostName(this.sessionRedis.getHost());
            factory.setPort(this.sessionRedis.getPort());
            if (this.sessionRedis.getPassword() != null) {
                factory.setPassword(this.sessionRedis.getPassword());
            }
        }
    }

    private void configureConnectionFromUrl(JedisConnectionFactory factory) {
        String url = this.sessionRedis.getUrl();
        if (url.startsWith("rediss://")) {
            factory.setUseSsl(true);
        }
        try {
            URI uri = new URI(url);
            factory.setHostName(uri.getHost());
            factory.setPort(uri.getPort());
            if (uri.getUserInfo() != null) {
                String password = uri.getUserInfo();
                int index = password.lastIndexOf(":");
                if (index >= 0) {
                    password = password.substring(index + 1);
                }
                factory.setPassword(password);
            }
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException("Malformed 'spring.redis.url' " + url, ex);
        }
    }

    private JedisConnectionFactory createJedisConnectionFactory() {
        JedisPoolConfig poolConfig = this.sessionRedis.getPool() != null
                ? jedisPoolConfig() : new JedisPoolConfig();
        if (getSentinelConfig() != null) {
            return new JedisConnectionFactory(getSentinelConfig(), poolConfig);
        }
        if (getClusterConfiguration() != null) {
            return new JedisConnectionFactory(getClusterConfiguration(), poolConfig);
        }
        return new JedisConnectionFactory(poolConfig);
    }

    private JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        RedisProperties.Pool props = this.sessionRedis.getPool();
        config.setMaxTotal(props.getMaxActive());
        config.setMaxIdle(props.getMaxIdle());
        config.setMinIdle(props.getMinIdle());
        config.setMaxWaitMillis(props.getMaxWait());
        return config;
    }

    private RedisSentinelConfiguration getSentinelConfig() {
        RedisProperties.Sentinel sentinelProperties = this.sessionRedis.getSentinel();
        if (sentinelProperties != null) {
            RedisSentinelConfiguration config = new RedisSentinelConfiguration();
            config.master(sentinelProperties.getMaster());
            config.setSentinels(createSentinels(sentinelProperties));
            return config;
        }
        return null;
    }

    private RedisClusterConfiguration getClusterConfiguration() {
        if (this.sessionRedis.getCluster() == null) {
            return null;
        }
        RedisProperties.Cluster clusterProperties = this.sessionRedis.getCluster();
        RedisClusterConfiguration config = new RedisClusterConfiguration(clusterProperties.getNodes());
        if (clusterProperties.getMaxRedirects() != null) {
            config.setMaxRedirects(clusterProperties.getMaxRedirects());
        }
        return config;
    }

    private List<RedisNode> createSentinels(RedisProperties.Sentinel sentinel) {
        List<RedisNode> nodes = new ArrayList<>();
        for (String node : StringUtils.commaDelimitedListToStringArray(sentinel.getNodes())) {
            try {
                String[] parts = StringUtils.split(node, ":");
                Assert.state(parts.length == 2, "Must be defined as 'host:port'");
                nodes.add(new RedisNode(parts[0], Integer.valueOf(parts[1])));
            } catch (RuntimeException ex) {
                throw new IllegalStateException("Invalid redis sentinel " + "property '" + node + "'", ex);
            }
        }
        return nodes;
    }

}
