package com.backend.tripod.web.component;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class AccessRecord {
    private String userId;
    private String sessionId;
    private String url;
    private String ip;
    private String userAgent;
    private String referer;
    private Date time;
}
