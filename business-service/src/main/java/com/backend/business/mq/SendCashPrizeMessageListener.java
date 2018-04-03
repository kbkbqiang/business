package com.backend.business.mq;

import com.backend.business.constant.BusinessRabbitConstant;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @Description 发送现金红包mq
 * @Author zq
 * @Date 2017/12/5
 */
//@Component
public class SendCashPrizeMessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(SendCashPrizeMessageListener.class);

    @RabbitListener(queues = {BusinessRabbitConstant.Queue.SEND_CASH_PRIZE_QUEUE})
    public void onSendCashPrizeMessage(String userId, Message message, Channel channel) throws IOException {
        LOGGER.info("收到发送红包MQ消息,userId={}", userId);
        try {
            System.out.println("================================");
        } catch (Exception e) {
            LOGGER.error("发送红包错误：", e);
            channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
        } finally {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }
    }
}
