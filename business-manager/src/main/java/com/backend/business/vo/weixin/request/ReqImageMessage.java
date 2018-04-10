package com.backend.business.vo.weixin.request;

/**
 * 图片消息
 *
 */
public class ReqImageMessage extends ReqBaseMessage {
	// 图片链接
	private String PicUrl;

	public String getPicUrl() {
		return PicUrl;
	}

	public void setPicUrl(String picUrl) {
		PicUrl = picUrl;
	}
}
