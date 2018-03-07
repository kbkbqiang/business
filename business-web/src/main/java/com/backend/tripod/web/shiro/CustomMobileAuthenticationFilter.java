package com.backend.tripod.web.shiro;

import com.alibaba.fastjson.JSON;
import com.backend.tripod.web.annotation.AuthIgnore;
import com.backend.tripod.web.util.RequestUtil;
import com.backend.tripod.web.vo.Error;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.CorsProcessor;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.cors.DefaultCorsProcessor;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class CustomMobileAuthenticationFilter extends FormAuthenticationFilter implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger logger = LoggerFactory.getLogger(CustomMobileAuthenticationFilter.class);
    private static final String NOT_LOGIN_ERROR = "BSC10005";   //用户未登录
    private static final String FILTER_NAME = CustomMobileAuthenticationFilter.class.getName();
    private static final String DEFAULT_STRATEGIES_PATH = "DispatcherServlet.properties";
    private static final Properties defaultStrategies;
    private final CorsProcessor corsProcessor = new DefaultCorsProcessor();
    private final UrlBasedCorsConfigurationSource corsConfigSource = new UrlBasedCorsConfigurationSource();
    private DefaultHandlerExceptionResolver exceptionResolver;

    private ApplicationContext context;
    private List<HandlerMapping> handlerMappings;
    private List<String> excludeAuthPaths = new ArrayList<>();

    /**
     * 是否是传统返回/渲染页面模式，如果是则可能需要在登录成功或失败后进行重定向
     * 否则就是app或者ajax方式请求，无需重定向处理，需要将控制权转给controller做业务处理，如封装登录返回信息
     */
    private boolean renderPageMode = false;

    static {
        try {
            ClassPathResource resource = new ClassPathResource(DEFAULT_STRATEGIES_PATH, DispatcherServlet.class);
            defaultStrategies = PropertiesLoaderUtils.loadProperties(resource);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not load 'DispatcherServlet.properties': " + ex.getMessage());
        }
    }

    public void setExcludeAuthPaths(List<String> excludeAuthPaths) {
        this.excludeAuthPaths = excludeAuthPaths;
    }

    public void setRenderPageMode(boolean renderPageMode) {
        this.renderPageMode = renderPageMode;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        this.context = event.getApplicationContext();
        this.exceptionResolver = context.getBean(DefaultHandlerExceptionResolver.class);
        initStrategies();
    }

    @Override
    public boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) throws Exception {
        return excludeAuthPath(request) || super.onPreHandle(request, response, mappedValue);
    }

    private boolean excludeAuthPath(ServletRequest request) {
        String uri = getPathWithinApplication(request);
        for (String excludeAuthPath : excludeAuthPaths) {
            if (pathMatcher.matches(excludeAuthPath, uri)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        if (renderPageMode) {
            return super.onAccessDenied(servletRequest, servletResponse);
        } else {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            Pair<HandlerExecutionChain, HandlerMapping> pair;
            try {
                pair = getHandlerPair(request);
            } catch (Exception e) {
                exceptionResolver.resolveException(request, response, null, e);
                return false;
            }
            Object handler = pair.getLeft().getHandler();
            if (handler instanceof HandlerMethod) {
                HandlerMethod handlerMethod = (HandlerMethod) handler;
                //整个controller类是否都忽略认证&授权
                Class<?> beanType = handlerMethod.getBeanType();
                if (beanType.isAnnotationPresent(AuthIgnore.class)) {
                    return true;
                }
                //当前请求方法是否都忽略认证&授权
                Method method = handlerMethod.getMethod();
                if (method.isAnnotationPresent(AuthIgnore.class)) {
                    return true;
                }
                logger.warn("用户未登录:{}", RequestUtil.traceRequest(request));
                processCors((RequestMappingHandlerMapping) pair.getRight(), handler, request, response);
                Error error = new Error(NOT_LOGIN_ERROR, "用户未登录");
                response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
                response.getWriter().print(JSON.toJSONString(error));
                return false;
            }
            return true;
        }
    }

    private void processCors(RequestMappingHandlerMapping handlerMapping, Object handler, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (CorsUtils.isCorsRequest(request)) {
            if (corsConfigSource.getCorsConfigurations().isEmpty()) {
                //Set "global" CORS configuration
                corsConfigSource.setCorsConfigurations(handlerMapping.getCorsConfigurations());
            }
            CorsConfiguration globalConfig = corsConfigSource.getCorsConfiguration(request);
            CorsConfiguration handlerConfig = getCorsConfiguration(handler, request);
            CorsConfiguration config = (globalConfig != null ? globalConfig.combine(handlerConfig) : handlerConfig);
            corsProcessor.processRequest(config, request, response);
        }
    }

    private CorsConfiguration getCorsConfiguration(Object handler, HttpServletRequest request) {
        if (handler instanceof HandlerExecutionChain) {
            handler = ((HandlerExecutionChain) handler).getHandler();
        }
        if (handler instanceof CorsConfigurationSource) {
            return ((CorsConfigurationSource) handler).getCorsConfiguration(request);
        }
        return null;
    }

    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {
        return !renderPageMode || super.onLoginSuccess(token, subject, request, response);
    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        return super.onLoginFailure(token, e, request, response);
    }

    private Pair<HandlerExecutionChain, HandlerMapping> getHandlerPair(HttpServletRequest request) throws Exception {
        for (HandlerMapping hm : this.handlerMappings) {
            HandlerExecutionChain handler = hm.getHandler(request);
            if (handler != null) {
                return new ImmutablePair<>(handler, hm);
            }
        }
        throw new NoHandlerFoundException(request.getMethod(), request.getRequestURI(), new ServletServerHttpRequest(request).getHeaders());
    }

    private void initStrategies() {
        initHandlerMappings();
    }

    private void initHandlerMappings() {
        this.handlerMappings = null;
        Map<String, HandlerMapping> matchingBeans = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
        if (!matchingBeans.isEmpty()) {
            this.handlerMappings = new ArrayList<>(matchingBeans.values());
            AnnotationAwareOrderComparator.sort(this.handlerMappings);
        }
        if (this.handlerMappings == null) {
            this.handlerMappings = getDefaultStrategies(HandlerMapping.class);
            if (logger.isDebugEnabled()) {
                logger.debug("No HandlerMappings found in servlet '" + FILTER_NAME + "': using default");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> getDefaultStrategies(Class<T> strategyInterface) {
        String key = strategyInterface.getName();
        String value = defaultStrategies.getProperty(key);
        if (value != null) {
            String[] classNames = StringUtils.commaDelimitedListToStringArray(value);
            List<T> strategies = new ArrayList<>(classNames.length);
            for (String className : classNames) {
                try {
                    Class<?> clazz = ClassUtils.forName(className, DispatcherServlet.class.getClassLoader());
                    Object strategy = createDefaultStrategy(clazz);
                    strategies.add((T) strategy);
                } catch (ClassNotFoundException ex) {
                    throw new BeanInitializationException(
                            "Could not find DispatcherServlet's default strategy class [" + className + "] for interface [" + key + "]", ex);
                } catch (LinkageError err) {
                    throw new BeanInitializationException(
                            "Error loading DispatcherServlet's default strategy class [" + className + "] for interface [" + key + "]: problem with class file or dependent class", err);
                }
            }
            return strategies;
        } else {
            return new LinkedList<>();
        }
    }

    private Object createDefaultStrategy(Class<?> clazz) {
        return context.getAutowireCapableBeanFactory().createBean(clazz);
    }

}
