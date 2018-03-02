package com.backend.business.web.interceptor;

import com.backend.business.web.component.AccessRecord;
import com.backend.business.web.component.AccessRecorder;
import com.backend.business.web.shiro.UserAuthPrincipal;
import com.backend.business.web.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Slf4j
public class AccessRecordInterceptor implements HandlerInterceptor {

    private final AccessRecorder recorder;

    public AccessRecordInterceptor(AccessRecorder recorder) {
        this.recorder = recorder;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try {
            recordAccess(request);
        } catch (Exception e) {
            log.error("record access error.", e);
        }
        return true;
    }

    private void recordAccess(HttpServletRequest request) {
        AccessRecord record = new AccessRecord();
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession(false);
        if (session != null) {
            String sessionId = session.getId().toString();
            UserAuthPrincipal principal = (UserAuthPrincipal) subject.getPrincipal();
            String userId = principal.getUserId();
            record.setUserId(userId);
            record.setSessionId(sessionId);
        }
        record.setUrl(request.getRequestURL().toString());
        record.setIp(RequestUtil.getUserRealIP(request));
        record.setUserAgent(request.getHeader(HttpHeaders.USER_AGENT));
        record.setReferer(request.getHeader(HttpHeaders.REFERER));
        record.setTime(new Date());
        recorder.record(record);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
