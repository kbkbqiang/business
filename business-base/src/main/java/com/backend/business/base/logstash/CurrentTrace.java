package com.backend.business.base.logstash;


public class CurrentTrace {

    public static final ThreadLocal<String> currentTraceId = ThreadLocal.withInitial(() -> "");
    public static final ThreadLocal<String> currentSpanId = ThreadLocal.withInitial(() -> "");

    public static void setCurrentTraceId(String traceId) {
        currentTraceId.set(traceId);
    }

    public static void setCurrentSpanId(String spanId) {
        currentSpanId.set(spanId);
    }

    public static String getCurrentTraceId() {
        return currentTraceId.get();
    }

    public static String getCurrentSpanId() {
        return currentSpanId.get();
    }

}
