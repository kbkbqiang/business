package com.backend.tripod.rabbitmq.configuration;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.ErrorHandler;

@Configuration
@EnableConfigurationProperties({RabbitProperties.class})
@EnableAspectJAutoProxy(exposeProxy = true)
public class TripodRabbitConfiguration {

    @Autowired
    private ObjectProvider<MessageConverter> messageConverter;

    @Autowired
    private RabbitProperties properties;

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    @Bean
    public Jackson2JsonMessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @ConditionalOnMissingBean(name = "rabbitTemplate")
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        initTemplate(template);
        return template;
    }

    void initTemplate(RabbitTemplate template) {
        MessageConverter messageConverter = this.messageConverter.getIfUnique();
        if (messageConverter != null) {
            template.setMessageConverter(messageConverter);
        }
        template.setMandatory(determineMandatoryFlag());
        RabbitProperties.Template templateProperties = this.properties.getTemplate();
        RabbitProperties.Retry retryProperties = templateProperties.getRetry();
        if (retryProperties.isEnabled()) {
            template.setRetryTemplate(createRetryTemplate(retryProperties));
        }
        if (templateProperties.getReceiveTimeout() != null) {
            template.setReceiveTimeout(templateProperties.getReceiveTimeout());
        }
        if (templateProperties.getReplyTimeout() != null) {
            template.setReplyTimeout(templateProperties.getReplyTimeout());
        }
    }

    private boolean determineMandatoryFlag() {
        Boolean mandatory = this.properties.getTemplate().getMandatory();
        return (mandatory != null ? mandatory : this.properties.isPublisherReturns());
    }

    private RetryTemplate createRetryTemplate(RabbitProperties.Retry properties) {
        RetryTemplate template = new RetryTemplate();
        SimpleRetryPolicy policy = new SimpleRetryPolicy();
        policy.setMaxAttempts(properties.getMaxAttempts());
        template.setRetryPolicy(policy);
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(properties.getInitialInterval());
        backOffPolicy.setMultiplier(properties.getMultiplier());
        backOffPolicy.setMaxInterval(properties.getMaxInterval());
        template.setBackOffPolicy(backOffPolicy);
        return template;
    }

    @Bean
    @ConditionalOnMissingBean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        configurer.configure(factory, connectionFactory);
        factory.setErrorHandler(errorHandler());
        return factory;
    }

    @Bean
    public ErrorHandler errorHandler() {
        return new ConditionalRejectingErrorHandler(new ConditionalRejectingErrorHandler.DefaultExceptionStrategy() {
            @Override
            public boolean isFatal(Throwable t) {
                return super.isFatal(t);
            }

            @Override
            protected boolean isUserCauseFatal(Throwable cause) {
                return super.isUserCauseFatal(cause);
            }
        });
    }

}
