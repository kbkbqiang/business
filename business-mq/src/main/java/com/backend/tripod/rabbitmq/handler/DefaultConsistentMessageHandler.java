package com.backend.tripod.rabbitmq.handler;

import com.backend.tripod.rabbitmq.entity.RabbitMessage;
import com.backend.tripod.rabbitmq.enums.MessageStatus;
import com.backend.tripod.service.properties.ShardingDataSourceProperties;
import com.dangdang.ddframe.rdb.sharding.api.HintManager;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.aop.framework.AopContext;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DefaultConsistentMessageHandler implements ConsistentMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(DefaultConsistentMessageHandler.class);

    private final JdbcTemplate jdbcTemplate;
    private final ShardingDataSourceProperties shardingDataSourceProperties;

    public DefaultConsistentMessageHandler(JdbcTemplate jdbcTemplate, ShardingDataSourceProperties shardingDataSourceProperties) {
        this.jdbcTemplate = jdbcTemplate;
        this.shardingDataSourceProperties = shardingDataSourceProperties;
    }

    @Override
    public String getNameSpace() {
        HintManager.getInstance().setMasterRouteOnly();
        String defaultMasterConnectionURL;
        if (shardingDataSourceProperties.isEnabled()) {
            defaultMasterConnectionURL = getDefaultAndMasterConnectionURL(shardingDataSourceProperties);
        } else {
            defaultMasterConnectionURL = jdbcTemplate.execute((ConnectionCallback<String>) con -> {
                PreparedStatement statement = con.prepareStatement("UPDATE `t_mq_send` SET `remark` = '' WHERE 1 = 2");
                statement.executeUpdate();
                return con.getMetaData().getURL();
            });
        }
        Assert.isTrue(StringUtils.isNotBlank(defaultMasterConnectionURL), "can not found default master datasource url");
        return getURLIdentifier(defaultMasterConnectionURL);
    }

    private String getURLIdentifier(String url) {
        if (url.contains("?")) {
            return url.substring(0, url.indexOf("?")).replaceAll("/", "#").trim();
        } else {
            return url.replaceAll("/", "#").trim();
        }
    }

    private String getDefaultAndMasterConnectionURL(ShardingDataSourceProperties shardingDataSourceProperties) {
        String defaultDataSourceName = shardingDataSourceProperties.getDefaultDataSourceName();
        Map<String, ShardingDataSourceProperties.DataSourceInstance> map = shardingDataSourceProperties.getDataSources();
        Set<Map.Entry<String, ShardingDataSourceProperties.DataSourceInstance>> entrySet = map.entrySet();
        for (Map.Entry<String, ShardingDataSourceProperties.DataSourceInstance> entry : entrySet) {
            String key = entry.getKey();
            if (defaultDataSourceName.equals(key)) {
                return entry.getValue().getMaster().getUrl();
            }
        }
        return null;
    }

    @Override
    @Transactional
    public void consistentPersist(RabbitMessage message) {
        String sql = "INSERT INTO `t_mq_send` (`id`, `exchange`, `routing_key`, `message`, `status`, `remark`) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, message.getId(), message.getExchange(), message.getRoutingKey(), message.getMessage(), MessageStatus.WAITING_FOR_SEND.ordinal(), "");
    }

    @Override
    public RabbitMessage queryById(String id) {
        return jdbcTemplate.queryForObject("", (rs, rowNum) -> {
            if (rs.next()) {
                return row2Object(rs);
            }
            return null;
        });
    }

    private RabbitMessage row2Object(ResultSet rs) throws SQLException {
        RabbitMessage msg = new RabbitMessage();
        String _id = rs.getString("id");
        String _exchange = rs.getString("exchange");
        String _routing_key = rs.getString("routing_key");
        String _message = rs.getString("message");
        msg.setId(_id);
        msg.setExchange(_exchange);
        msg.setRoutingKey(_routing_key);
        msg.setMessage(_message);
        return msg;
    }

    @Override
    public List<RabbitMessage> queryForSendList() {
        DateTime now = new DateTime();
        DateTime _3daysAgo = now.minusDays(10);
        DateTime _15SecondsAgo = now.minusSeconds(15);
        return jdbcTemplate.query("SELECT * FROM `t_mq_send` WHERE `status` < ? AND `create_time` > ? AND `create_time` < ? AND `retried_times` < ?",
                new Object[]{MessageStatus.SEND_SUCCESS.ordinal(), _3daysAgo.toDate(), _15SecondsAgo.toDate(), 4},
                (rs, rowNum) -> row2Object(rs));
    }

    @Override
    @Transactional
    public void updateById(String id, MessageStatus status, String remark) {
        jdbcTemplate.update("UPDATE `t_mq_send` SET `status` = ?, `retried_times` = `retried_times` + 1, `remark` = ? WHERE `id` = ?", status.ordinal(), remark, id);
    }

    @Override
    @Transactional
    public void deleteById(String id) {
        jdbcTemplate.update("DELETE FROM `t_mq_send` WHERE `id` = ?", id);
    }

    @Override
    @Transactional
    public void deleteBatchByStatus(MessageStatus status) {
        jdbcTemplate.update("DELETE FROM `t_mq_send` WHERE `status` = ?", status.ordinal());
    }

    @Override
    @Transactional
    public void onConfirm(CorrelationData correlationData, boolean ack, String cause) {
        String id = correlationData.getId();
        logger.info("publish confirm ====> id={}, ack={}, cause={}", id, ack, cause);
        ConsistentMessageHandler proxy = (ConsistentMessageHandler) AopContext.currentProxy();
        if (ack) {
            proxy.deleteById(id);
        } else {
            proxy.updateById(id, MessageStatus.SEND_ERROR, cause);
        }
    }

}
