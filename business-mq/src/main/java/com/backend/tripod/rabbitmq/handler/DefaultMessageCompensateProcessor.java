package com.backend.tripod.rabbitmq.handler;

import com.backend.tripod.rabbitmq.component.ConsistencyRabbitTemplate;
import com.backend.tripod.rabbitmq.entity.RabbitMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DefaultMessageCompensateProcessor implements MessageCompensateProcessor {

    private final InterProcessMutex lock;
    private final ConsistencyRabbitTemplate rabbitTemplate;
    private final ConsistentMessageHandler consistentMessageHandler;

    public DefaultMessageCompensateProcessor(CuratorFramework curatorFramework, ConsistencyRabbitTemplate rabbitTemplate, ConsistentMessageHandler consistentMessageHandler) {
        this.rabbitTemplate = rabbitTemplate;
        this.consistentMessageHandler = consistentMessageHandler;
        this.lock = new InterProcessMutex(curatorFramework, "/niiwoo/mq-compensate-lock/" + consistentMessageHandler.getNameSpace());
    }

    @Override
    public void compensate() {
        long timeoutSeconds = 5;
        boolean acquire = false;
        try {
            acquire = lock.acquire(timeoutSeconds, TimeUnit.SECONDS);
            if (acquire) {
                List<RabbitMessage> messageList = consistentMessageHandler.queryForSendList();
                for (RabbitMessage msg : messageList) {
                    rabbitTemplate.compensateSend(msg);
                }
                Thread.sleep((timeoutSeconds + 2) * 1000);
            }
        } catch (Exception e) {
            log.error("补偿发送MQ消息执行失败", e);
        } finally {
            try {
                if (acquire) {
                    lock.release();
                }
            } catch (Exception e) {
                log.error("补偿发送MQ消息释放锁失败", e);
            }
        }
    }

}
