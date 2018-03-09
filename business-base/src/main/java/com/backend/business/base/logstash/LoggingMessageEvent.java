package com.backend.business.base.logstash;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.ThrowableProxy;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Marker;

import java.util.Map;

public class LoggingMessageEvent implements ILoggingEvent {

    private ILoggingEvent loggingEvent;
    private String traceId;
    private String spanId;
    private String userId;
    private String sessionId;

    public LoggingMessageEvent(ILoggingEvent loggingEvent, String userId, String sessionId, String traceId, String spanId) {
        this.loggingEvent = loggingEvent;
        this.userId = userId;
        this.sessionId = sessionId;
        this.traceId = traceId;
        this.spanId = spanId;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public ILoggingEvent getLoggingEvent() {
        return loggingEvent;
    }

    public String getUserId() {
        return userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    @Override
    public String toString() {
        return loggingEvent.toString();
    }

    @Override
    public String getThreadName() {
        return loggingEvent.getThreadName();
    }

    @Override
    public Level getLevel() {
        return loggingEvent.getLevel();
    }

    @Override
    public String getMessage() {
        String exceptionMessage = determineExceptionMessage();
        if (exceptionMessage != null) {
            return loggingEvent.getMessage() + System.getProperty("line.separator", "\n") + exceptionMessage;
        } else {
            return loggingEvent.getMessage();
        }
    }

    @Override
    public Object[] getArgumentArray() {
        return loggingEvent.getArgumentArray();
    }

    @Override
    public String getFormattedMessage() {
        String exceptionMessage = determineExceptionMessage();
        if (exceptionMessage != null) {
            return loggingEvent.getFormattedMessage() + System.getProperty("line.separator", "\n") + exceptionMessage;
        } else {
            return loggingEvent.getFormattedMessage();
        }
    }

    private String determineExceptionMessage() {
        IThrowableProxy throwableProxy = loggingEvent.getThrowableProxy();
        if (throwableProxy instanceof ThrowableProxy) {
            ThrowableProxy proxy = (ThrowableProxy) throwableProxy;
            Throwable throwable = proxy.getThrowable();
            if (throwable != null) {
                return ExceptionUtils.getStackTrace(throwable);
            }
        }
        return null;
    }

    @Override
    public String getLoggerName() {
        return loggingEvent.getLoggerName();
    }

    @Override
    public LoggerContextVO getLoggerContextVO() {
        return loggingEvent.getLoggerContextVO();
    }

    @Override
    public IThrowableProxy getThrowableProxy() {
        return loggingEvent.getThrowableProxy();
    }

    @Override
    public StackTraceElement[] getCallerData() {
        return loggingEvent.getCallerData();
    }

    @Override
    public boolean hasCallerData() {
        return loggingEvent.hasCallerData();
    }

    @Override
    public Marker getMarker() {
        return loggingEvent.getMarker();
    }

    @Override
    public Map<String, String> getMDCPropertyMap() {
        return loggingEvent.getMDCPropertyMap();
    }

    @Override
    public Map<String, String> getMdc() {
        return loggingEvent.getMdc();
    }

    @Override
    public long getTimeStamp() {
        return loggingEvent.getTimeStamp();
    }

    @Override
    public void prepareForDeferredProcessing() {
        loggingEvent.prepareForDeferredProcessing();
    }
}
