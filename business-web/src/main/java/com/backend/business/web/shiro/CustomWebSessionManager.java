package com.backend.business.web.shiro;

import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;

public class CustomWebSessionManager extends DefaultWebSessionManager {

    @Override
    protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
        Serializable sessionId;
        if (request instanceof HttpServletRequest) {
            sessionId = ((HttpServletRequest) request).getHeader("token");
            if (sessionId == null) {
                sessionId = request.getParameter("token");
            }
        } else {
            sessionId = super.getSessionId(request, response);
        }
        return sessionId;
    }

}
