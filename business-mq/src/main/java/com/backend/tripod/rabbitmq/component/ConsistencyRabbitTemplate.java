package com.backend.tripod.rabbitmq.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.backend.tripod.rabbitmq.entity.RabbitMessage;
import com.backend.tripod.rabbitmq.handler.ConsistentMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

public class ConsistencyRabbitTemplate extends RabbitTemplate {

    private static final Logger logger = LoggerFactory.getLogger(ConsistencyRabbitTemplate.class);

    private final ConsistentMessageHandler consistentMessageHandler;

    public ConsistencyRabbitTemplate(ConnectionFactory connectionFactory, ConsistentMessageHandler consistentMessageHandler) {
        super(connectionFactory);
        this.consistentMessageHandler = consistentMessageHandler;
    }

    /**
     * 补偿发送
     *
     * @param msg
     * @throws AmqpException
     */
    public void compensateSend(final RabbitMessage msg) throws AmqpException {
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        Message realMessage = new Message(msg.getMessage().getBytes(), properties);
        try {
            super.send(msg.getExchange(), msg.getRoutingKey(), realMessage, new CorrelationData(msg.getId()));
            if (logger.isInfoEnabled()) {
                logger.info("补偿发送MQ消息,message={}", msg.getMessage(), "utf-8");
            }
        } catch (Exception e) {
            logger.error("compensate send message to rabbit error: message={}, cause={}", msg.getMessage(), e);
        }
    }

    @Override
    public void send(final String exchange, final String routingKey, final Message message, CorrelationData correlationData) throws AmqpException {
        if (correlationData == null) {
            correlationData = new CorrelationData(UUID.randomUUID().toString());
        }
        //消息入库
        RabbitMessage msg = new RabbitMessage();
        msg.setId(correlationData.getId());
        msg.setExchange(exchange);
        msg.setRoutingKey(routingKey);
        msg.setMessage(new String(message.getBody()));
        consistentMessageHandler.consistentPersist(msg);
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            try {
                super.send(exchange, routingKey, message, correlationData);
                if (logger.isInfoEnabled()) {
                    logger.info("发送MQ消息,message={}", JSON.toJSONString(msg, SerializerFeature.UseSingleQuotes));
                }
            } catch (Exception e) {
                logger.error("send message to rabbit error: message={}, cause={}", JSON.toJSONString(msg, SerializerFeature.UseSingleQuotes), e);
            }
        } else {
            sendAfterTransactionCompletion(msg);
        }
    }

    private void sendAfterTransactionCompletion(final RabbitMessage msg) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {

            @Override
            public void afterCompletion(int status) {
                super.afterCompletion(status);
                if (TransactionSynchronization.STATUS_COMMITTED == status) {
                    try {
                        MessageProperties properties = new MessageProperties();
                        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
                        ConsistencyRabbitTemplate.super.send(msg.getExchange(), msg.getRoutingKey(), new Message(msg.getMessage().getBytes(), properties), new CorrelationData(msg.getId()));
                        if (logger.isInfoEnabled()) {
                            logger.info("发送MQ消息,message={}", JSON.toJSONString(msg, SerializerFeature.UseSingleQuotes));
                        }
                    } catch (Exception e) {
                        logger.error("send message to rabbit error: message={}, cause={}", JSON.toJSONString(msg, SerializerFeature.UseSingleQuotes), e);
                    }
                } else {
                    consistentMessageHandler.deleteById(msg.getId());
                    logger.info("delete rabbit message from db, cause transaction status={},message={}", status, JSON.toJSONString(msg, SerializerFeature.UseSingleQuotes));
                }
            }
        });
    }

}
