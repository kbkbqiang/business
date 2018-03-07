CREATE TABLE `t_mq_send` (
  `id`          VARCHAR(40)  NOT NULL,
  `exchange`    VARCHAR(128) NOT NULL,
  `routing_key` VARCHAR(128)          DEFAULT NULL,
  `message`     LONGTEXT     NOT NULL,
  `status`      TINYINT(4)   NOT NULL,
  `remark`      VARCHAR(128)          DEFAULT NULL,
  `create_time` DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME(3)           DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8