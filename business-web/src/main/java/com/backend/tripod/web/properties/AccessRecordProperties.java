package com.backend.tripod.web.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("access-record")
@Setter
@Getter
public class AccessRecordProperties {
    private boolean enabled = true;
    private String tableName;
}
