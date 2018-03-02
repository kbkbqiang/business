package com.backend.business.web.interceptor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.backend.business.component.ReloadableMessageSource;
import com.backend.business.helper.BeanFactoryHelper;
import com.backend.business.web.annotation.AuthIgnore;
import com.backend.business.web.enums.DeviceTypeEnum;
import com.backend.business.web.shiro.KickOffRememberMeManager;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

public class KickOffInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(KickOffInterceptor.class);
    //    private final FastDateFormat dateFormat = FastDateFormat.getInstance("yyyy-MM-dd HH:mm");
    private ReloadableMessageSource reloadableMessageSource = BeanFactoryHelper.getBean(ReloadableMessageSource.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            //整个controller类是否都忽略认证&授权
            Class<?> beanType = handlerMethod.getBeanType();
            if (beanType.isAnnotationPresent(AuthIgnore.class)) {
                return true;
            }
            //当前请求方法是否都忽略认证&授权
            Method method = handlerMethod.getMethod();
            if (method.isAnnotationPresent(AuthIgnore.class)) {
                return true;
            }
            Subject subject = SecurityUtils.getSubject();
            Session session = subject.getSession(false);
            if (session != null) {
                Long kickTime = (Long) session.getAttribute(KickOffRememberMeManager.SessionKey.KICK_TIME);
                DeviceTypeEnum kickedByDevice = (DeviceTypeEnum) session.getAttribute(KickOffRememberMeManager.SessionKey.KICKED_BY_DEVICE);
                if (kickTime != null && kickedByDevice != null) {
                    //                    String time = dateFormat.format(kickTime);
                    String code = "BSC20001";
                    String message = reloadableMessageSource.getMessage(code);
                    //                    switch (kickedByDevice) {
                    //                        case ANDROID_MOBILE:
                    //                            message = "你的账号于" + time + "在一台Android手机登录。";
                    //                            break;
                    //                        case IOS_MOBILE:
                    //                            message = "你的账号于" + time + "在一台iPhone手机登录。";
                    //                            break;
                    //                        case H5:
                    //                            message = "你的账号于" + time + "在网页登录。";
                    //                            break;
                    //                        default:
                    //                            break;
                    //                    }
                    com.backend.business.web.vo.Error error = new com.backend.business.web.vo.Error(code, message);
                    logger.info("kickoff:uri={},principal={},reason={}", request.getRequestURI(), JSON.toJSONString(subject.getPrincipal(), SerializerFeature
                            .UseSingleQuotes), message);
                    response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
                    response.getWriter().write(JSON.toJSONString(error));
                    subject.logout();
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
