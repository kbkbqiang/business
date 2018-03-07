package com.backend.tripod.rabbitmq.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageStatus {
    WAITING_FOR_SEND("待发送"),
    SEND_ERROR("发送失败"),
    SEND_SUCCESS("发送成功");
    private String desc;
}
