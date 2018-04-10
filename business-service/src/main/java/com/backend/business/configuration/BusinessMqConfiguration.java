package com.backend.business.configuration;

import com.backend.business.constant.BusinessRabbitConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 所有mq定义配置
 *
 */
//@Configuration
public class BusinessMqConfiguration {


    //**********************
    //发送红包配置
    //**********************
    @Bean
    public Exchange sendCashPrizeExchange() {
        return new DirectExchange(BusinessRabbitConstant.Exchange.SEND_CASH_PRIZE_EXCHANGE, true, true);
    }

    @Bean
    public Queue sendCashPrizeQueue() {
        return new Queue(BusinessRabbitConstant.Queue.SEND_CASH_PRIZE_QUEUE, true);
    }

    @Bean
    public Binding sendCashPrizeBinding() {
        return BindingBuilder.bind(sendCashPrizeQueue()).to(sendCashPrizeExchange()).with(BusinessRabbitConstant.Key.SEND_CASH_PRIZE_ROUTINGKEY).noargs();
    }

}
