package com.backend.business.vo.weixin.request;

/**
 * 菜单事件
 *
 * @author zhaoqiang
 *
 */
public class ReqMenuMessage extends ReqBaseMessage {
	private String EventKey;

    public String getEventKey() {
        return EventKey;
    }

    public void setEventKey(String eventKey) {
        EventKey = eventKey;
    }
}
