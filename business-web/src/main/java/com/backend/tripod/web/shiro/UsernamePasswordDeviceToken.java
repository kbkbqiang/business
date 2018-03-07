package com.backend.tripod.web.shiro;

import com.backend.tripod.web.enums.DeviceTypeEnum;
import org.apache.shiro.authc.UsernamePasswordToken;

public class UsernamePasswordDeviceToken extends UsernamePasswordToken{
    private static final long serialVersionUID = -6986234946672320990L;

    private DeviceTypeEnum deviceType;

    public UsernamePasswordDeviceToken(String username, String password, DeviceTypeEnum deviceType) {
        super(username, password);
        this.deviceType = deviceType;
    }

    public DeviceTypeEnum getDeviceType() {
        return deviceType;
    }

}
