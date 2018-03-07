package com.backend.tripod.web.shiro;

import com.backend.tripod.web.enums.DeviceTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.shiro.authc.HostAuthenticationToken;
import org.apache.shiro.authc.RememberMeAuthenticationToken;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ThirdOpenIdToken implements HostAuthenticationToken, RememberMeAuthenticationToken {
    private static final long serialVersionUID = -6986234942342320990L;

    private String openId;
    private Byte platformId;
    private DeviceTypeEnum deviceType;
    private boolean rememberMe = false;
    private String host;

    public ThirdOpenIdToken(String openId, Byte platformId, DeviceTypeEnum deviceType) {
        this.openId = openId;
        this.platformId = platformId;
        this.deviceType = deviceType;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public boolean isRememberMe() {
        return rememberMe;
    }

    @Override
    public Object getPrincipal() {
        return getOpenId();
    }

    @Override
    public Object getCredentials() {
        return getPlatformId().toString();
    }

    public DeviceTypeEnum getDeviceType() {
        return deviceType;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName());
        sb.append(" - ");
        sb.append(openId);
        sb.append(", rememberMe=").append(rememberMe);
        if (host != null) {
            sb.append(" (").append(host).append(")");
        }
        return sb.toString();
    }
}
