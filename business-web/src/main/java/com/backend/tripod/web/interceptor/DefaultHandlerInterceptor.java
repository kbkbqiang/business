package com.backend.tripod.web.interceptor;

import brave.Tracer;
import brave.Tracing;
import com.alibaba.fastjson.JSONObject;
import com.backend.business.context.PrincipalFrameworkContext;
import com.backend.tripod.web.shiro.UserAuthPrincipal;
import com.backend.tripod.web.util.RequestUtil;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DefaultHandlerInterceptor implements HandlerInterceptor {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Tracer tracer;

    public DefaultHandlerInterceptor(Tracing tracing) {
        this.tracer = tracing.tracer();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String contentType = request.getContentType();
        if (contentType == null) {
            contentType = MediaType.TEXT_HTML_VALUE;
        }
        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession(false);
        if (session != null) {
            String sessionId = session.getId().toString();
            UserAuthPrincipal principal = (UserAuthPrincipal) subject.getPrincipal();
            String userId = principal.getUserId();
            PrincipalFrameworkContext.getContext().setSessionId(sessionId).setUserId(String.valueOf(userId));
        }
        if (!isJsonRequest(request)) {
            String url = request.getRequestURL().toString();
            JSONObject jsonObject = RequestUtil.traceRequest(request);
            JSONObject params = jsonObject.getJSONObject("params");
            JSONObject headers = jsonObject.getJSONObject("headers");
            if (logger.isInfoEnabled()) {
                logger.info("{} request<=========url:{} params:{}, headers:{}", contentType, url, params, headers);
            }
        }
        response.addHeader("traceId", tracer.currentSpan().context().traceIdString());
        return true;
    }

    private boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return MediaType.APPLICATION_JSON_VALUE.equalsIgnoreCase(contentType) || MediaType.APPLICATION_JSON_UTF8_VALUE.equalsIgnoreCase(contentType);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }

}
