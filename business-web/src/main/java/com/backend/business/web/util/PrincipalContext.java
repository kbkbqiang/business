package com.backend.business.web.util;

import com.backend.business.web.shiro.UserAuthPrincipal;
import com.google.common.base.Preconditions;
import org.apache.shiro.SecurityUtils;

import java.io.Serializable;

public class PrincipalContext {

    public static UserAuthPrincipal getUserPrincipal() {
        Object principal = SecurityUtils.getSubject().getPrincipal();
        //Preconditions.checkNotNull(principal, "user must be authenticated before call this method.");
        return (UserAuthPrincipal) principal;
    }

    public static String getUserId() {
        UserAuthPrincipal userPrincipal = getUserPrincipal();
        return null == userPrincipal ? null : userPrincipal.getUserId();
    }

    public static String getSessionId() {
        Serializable sessionId = SecurityUtils.getSubject().getSession(false).getId();
        Preconditions.checkNotNull(sessionId, "user must be authenticated before call this method.");
        return sessionId.toString();
    }

}
