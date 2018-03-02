package com.backend.business.web.shiro;

import com.backend.business.web.enums.DeviceTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class UserAuthPrincipal implements Serializable {
    private static final long serialVersionUID = -9184247409709140766L; //do not modify!!!

    /**
     * 身份
     */
    private String principal;
    /**
     * 凭证
     */
    private transient String credentials;
    private transient String salt;
    private String userId;
    /**
     * 设备类型
     */
    private DeviceTypeEnum deviceType;
    /**
     * 业务属性保留域
     */
    private String reserve;
}
