package com.backend.tripod.web.shiro;

import com.backend.tripod.web.enums.DeviceTypeEnum;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.mgt.AbstractRememberMeManager;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.SubjectContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by zhangwanli on 2017/12/11.
 */
public class KickOffRememberMeManager extends AbstractRememberMeManager {

    private static final Logger logger = LoggerFactory.getLogger(KickOffRememberMeManager.class);
    private final String tokenListKeyPrefix;
    private final SessionDAO sessionDAO;
    private final StringRedisTemplate redisTemplate;

    public KickOffRememberMeManager(String sessionIdPrefix, SessionDAO sessionDAO, StringRedisTemplate redisTemplate) {
        this.tokenListKeyPrefix = "token-list-of-" + sessionIdPrefix;
        this.sessionDAO = sessionDAO;
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void forgetIdentity(Subject subject) {

    }

    @Override
    protected void rememberSerializedIdentity(Subject subject, byte[] serialized) {

    }

    @Override
    protected byte[] getRememberedSerializedIdentity(SubjectContext subjectContext) {
        return new byte[0];
    }

    @Override
    public void forgetIdentity(SubjectContext subjectContext) {

    }

    @Override
    public void onSuccessfulLogin(Subject subject, AuthenticationToken token, AuthenticationInfo info) {
        super.onSuccessfulLogin(subject, token, info);
        Session session = subject.getSession();
        UserAuthPrincipal authPrincipal = (UserAuthPrincipal) subject.getPrincipal();
        String principal = authPrincipal.getPrincipal();
        DeviceTypeEnum currentDevice = authPrincipal.getDeviceType();
        session.setAttribute(SessionKey.DEVICE, currentDevice);
        //根据principal（登录账号）获取其在其他设备登录的session信息
        String userTokensKey = tokenListKeyPrefix + principal;
        List<String> previousTokenList = redisTemplate.opsForList().range(userTokensKey, 0, -1);
        logger.info("{}在{}设备上登录,token={},previousTokenList={}", principal, currentDevice.getDesc(), session.getId(), previousTokenList);
        for (String previousToken : previousTokenList) {
            Session previousSession = null;
            try {
                previousSession = sessionDAO.readSession(previousToken);
            } catch (UnknownSessionException e) {
                logger.warn("unknown token:{} maybe user already logout or expired.", previousToken);
            }
            if (previousSession != null) {
                DeviceTypeEnum previousDevice = (DeviceTypeEnum) previousSession.getAttribute(SessionKey.DEVICE);
                if (shouldTickOff(currentDevice, previousDevice)) {
                    previousSession.setAttribute(SessionKey.KICKED_BY_DEVICE, currentDevice);
                    previousSession.setAttribute(SessionKey.KICK_TIME, session.getStartTimestamp().getTime());
                    sessionDAO.update(previousSession); //非当前session需手动update session到dao
                    logger.info("{}之前在{}设备上的登录将被踢出,token={}", principal, previousDevice.getDesc(), previousSession.getId());
                    redisTemplate.opsForList().remove(userTokensKey, 1, previousToken);
                }
            } else {
                redisTemplate.opsForList().remove(userTokensKey, 1, previousToken);
            }
        }
        redisTemplate.opsForList().leftPush(userTokensKey, session.getId().toString());
        redisTemplate.expire(userTokensKey, session.getTimeout(), TimeUnit.MILLISECONDS);
    }

    private boolean shouldTickOff(DeviceTypeEnum currentDevice, DeviceTypeEnum previousDevice) {
        if (DeviceTypeEnum.H5 == currentDevice) {
            return false;
        }
        if (currentDevice == previousDevice) {
            return true;
        }
        if (DeviceTypeEnum.IOS_MOBILE == currentDevice) {
            return DeviceTypeEnum.ANDROID_MOBILE == previousDevice;
        }
        if (DeviceTypeEnum.ANDROID_MOBILE == currentDevice) {
            return DeviceTypeEnum.IOS_MOBILE == previousDevice;
        }
        return false;
    }

    public static class SessionKey {
        public static final String DEVICE = "device";   //设备类型
        public static final String KICKED_BY_DEVICE = "kicked_by_device";   //被什么设备踢出
        public static final String KICK_TIME = "kickTime";  //被踢时间
    }

}
