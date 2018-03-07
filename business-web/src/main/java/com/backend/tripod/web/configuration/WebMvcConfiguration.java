package com.backend.tripod.web.configuration;

import brave.SpanCustomizer;
import brave.Tracing;
import brave.http.HttpAdapter;
import brave.http.HttpClientParser;
import brave.http.HttpSampler;
import brave.http.HttpServerParser;
import brave.http.HttpTracing;
import brave.spring.webmvc.TracingHandlerInterceptor;
import com.backend.business.base.component.ReloadableMessageSource;
import com.backend.business.base.helper.BeanFactoryHelper;
import com.backend.tripod.web.advisor.RequestBodyAdvisor;
import com.backend.tripod.web.advisor.ResponseBodyAdvisor;
import com.backend.tripod.web.component.AccessRecorder;
import com.backend.tripod.web.convert.StringDateConvert;
import com.backend.tripod.web.interceptor.AccessRecordInterceptor;
import com.backend.tripod.web.interceptor.DefaultHandlerInterceptor;
import com.backend.tripod.web.interceptor.KickOffInterceptor;
import com.backend.tripod.web.properties.AccessRecordProperties;
import com.backend.tripod.web.resolver.CustomHandlerExceptionResolver;
import com.backend.tripod.web.shiro.UserAuthPrincipal;
import com.google.common.collect.Lists;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.AuthorizingSecurityManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.AsyncHandlerInterceptor;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@EnableConfigurationProperties({AccessRecordProperties.class})
public class WebMvcConfiguration extends DelegatingWebMvcConfiguration {

    private static final String[] CLASSPATH_RESOURCE_LOCATIONS = {
            "classpath:/META-INF/resources/",
            "classpath:/resources/",
            "classpath:/static/",
            "classpath:/public/"
    };

    @Autowired
    private ReloadableMessageSource reloadableMessageSource;

    @Override
    @Bean
    public RequestMappingHandlerAdapter requestMappingHandlerAdapter() {
        RequestMappingHandlerAdapter adapter = super.requestMappingHandlerAdapter();
        adapter.setRequestBodyAdvice(Lists.newArrayList(new RequestBodyAdvisor()));
        adapter.setResponseBodyAdvice(Lists.newArrayList(new ResponseBodyAdvisor()));
        return adapter;
    }

    @Override
    @Autowired(required = false)
    public void setConfigurers(List<WebMvcConfigurer> configurers) {
        if (configurers == null) {
            configurers = new ArrayList<>();
        }
        configurers.add(webMvcConfigurer());
        super.setConfigurers(configurers);
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultHandlerExceptionResolver defaultHandlerExceptionResolver() {
        return new CustomHandlerExceptionResolver(reloadableMessageSource);
    }

    @Bean
    public HandlerInterceptor defaultHandlerInterceptor(Tracing tracing) {
        return new DefaultHandlerInterceptor(tracing);
    }

    @Bean
    public KickOffInterceptor kickOffInterceptor() {
        return new KickOffInterceptor();
    }

    @Bean
    @ConditionalOnBean({AccessRecorder.class})
    public HandlerInterceptor accessRecordInterceptor(AccessRecorder recorder) {
        return new AccessRecordInterceptor(recorder);
    }

    @Bean
    @ConditionalOnBean(AuthorizingSecurityManager.class)
    public HttpTracing httpTracing(Tracing tracing) {
        return HttpTracing.newBuilder(tracing)
                .clientParser(new HttpClientParser() {
                    @Override
                    public <Req> void request(HttpAdapter<Req, ?> adapter, Req req, SpanCustomizer customizer) {
                        super.request(adapter, req, customizer);
                    }
                })
                .serverParser(new HttpServerParser() {
                    @Override
                    public <Req> void request(HttpAdapter<Req, ?> adapter, Req req, SpanCustomizer customizer) {
                        super.request(adapter, req, customizer);
                        Subject subject = SecurityUtils.getSubject();
                        Session session = subject.getSession(false);
                        if (session != null) {
                            UserAuthPrincipal principal = (UserAuthPrincipal) subject.getPrincipal();
                            customizer.tag("userId", principal.getUserId());
                            customizer.tag("sessionId", session.getId().toString());
                        }
                    }
                })
                .clientSampler(HttpSampler.TRACE_ID)
                .serverSampler(HttpSampler.TRACE_ID)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public AsyncHandlerInterceptor webServerTraceInterceptor(HttpTracing tracing) {
        return TracingHandlerInterceptor.create(tracing);
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                super.addInterceptors(registry);
                BeanFactory beanFactory = BeanFactoryHelper.getBeanFactory();
                List<HandlerInterceptor> interceptors = new ArrayList<>();
                if (beanFactory instanceof DefaultListableBeanFactory) {
                    Collection<HandlerInterceptor> values = ((DefaultListableBeanFactory) beanFactory).getBeansOfType(HandlerInterceptor.class).values();
                    interceptors.addAll(values);
                }
                interceptors.sort((o1, o2) -> {
                    if (o1 instanceof TracingHandlerInterceptor) {
                        return -1;
                    }
                    if (o2 instanceof TracingHandlerInterceptor) {
                        return 1;
                    }
                    return 0;
                });
                for (HandlerInterceptor interceptor : interceptors) {
                    registry.addInterceptor(interceptor).addPathPatterns("/**").excludePathPatterns("/error");
                }
            }

            @Override
            public void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
                super.configureHandlerExceptionResolvers(exceptionResolvers);
            }

            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                super.addResourceHandlers(registry);
                if (!registry.hasMappingForPattern("/webjars/**")) {
                    registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/", "classpath:/webjars/");
                }
                if (!registry.hasMappingForPattern("/**")) {
                    registry.addResourceHandler("/**").addResourceLocations(CLASSPATH_RESOURCE_LOCATIONS);
                }
                registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
                registry.addResourceHandler("/public/**").addResourceLocations("classpath:/public/");
                registry.addResourceHandler("/templates/**").addResourceLocations("classpath:/templates/");
            }

            @Override
            public void addFormatters(FormatterRegistry registry) {
                super.addFormatters(registry);
                registry.addConverter(new StringDateConvert());
            }

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                super.addCorsMappings(registry);
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedHeaders("*")
                        .allowedMethods("*")
                        .allowCredentials(true);
            }

        };
    }

}
