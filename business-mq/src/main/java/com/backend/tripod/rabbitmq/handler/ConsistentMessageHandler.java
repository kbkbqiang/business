package com.backend.tripod.rabbitmq.handler;

import com.backend.tripod.rabbitmq.entity.RabbitMessage;
import com.backend.tripod.rabbitmq.enums.MessageStatus;
import org.springframework.amqp.rabbit.support.CorrelationData;

import java.util.List;

/**
 * 本地事务与mq消息发送的一致性处理。即本地事务成功，mq发送消息也需保证成功
 */
public interface ConsistentMessageHandler {

    String getNameSpace();

    /**
     * 一致性存储消息。<p>
     * 发送mq之前与本地事务一致性存储待发送消息
     */
    void consistentPersist(RabbitMessage message);

    RabbitMessage queryById(String id);

    List<RabbitMessage> queryForSendList();

    void updateById(String id, MessageStatus status, String remark);

    void deleteById(String id);

    void deleteBatchByStatus(MessageStatus status);

    void onConfirm(CorrelationData correlationData, boolean ack, String cause);

}
