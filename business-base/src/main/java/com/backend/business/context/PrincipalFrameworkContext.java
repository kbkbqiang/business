package com.backend.business.context;

public class PrincipalFrameworkContext {

    public static final String USER_ID_KEY = "ctx-userid";
    public static final String SESSIONID_KEY = "ctx-sessionid";
    private static final ThreadLocal<PrincipalFrameworkContext> LOCAL = ThreadLocal.withInitial(PrincipalFrameworkContext::new);

    private String userId = "";
    private String sessionId = "";

    private PrincipalFrameworkContext() {

    }

    public static PrincipalFrameworkContext getContext() {
        return LOCAL.get();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public PrincipalFrameworkContext setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

}
