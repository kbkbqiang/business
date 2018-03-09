package com.backend.business.base.logstash;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.LoggerContextVO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Marker;

import java.util.Map;


public class TraceMessageEvent implements ILoggingEvent {

    private String nameSpace;
    private String type;
    private long timeStamp;
    private Object message;
    private JSONObject json;

    public TraceMessageEvent(String nameSpace, long timeStamp, Object message) {
        this.nameSpace = nameSpace;
        this.type = "trace";
        this.timeStamp = timeStamp;
        this.message = message;
        this.json = JSON.parseObject(message.toString());
    }

    public String getTraceId() {
        return json.getString("traceId");
    }

    public String getSpanId() {
        return json.getString("id");
    }

    public String getType() {
        return type;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    @Override
    public String getThreadName() {
        return null;
    }

    @Override
    public Level getLevel() {
        return Level.INFO;
    }

    @Override
    public String getMessage() {
        return message.toString();
    }

    public Object getRawMessage() {
        return message;
    }

    @Override
    public Object[] getArgumentArray() {
        return new Object[0];
    }

    @Override
    public String getFormattedMessage() {
        return null;
    }

    @Override
    public String getLoggerName() {
        return null;
    }

    @Override
    public LoggerContextVO getLoggerContextVO() {
        return null;
    }

    @Override
    public IThrowableProxy getThrowableProxy() {
        return null;
    }

    @Override
    public StackTraceElement[] getCallerData() {
        return new StackTraceElement[0];
    }

    @Override
    public boolean hasCallerData() {
        return false;
    }

    @Override
    public Marker getMarker() {
        return null;
    }

    @Override
    public Map<String, String> getMDCPropertyMap() {
        return null;
    }

    @Override
    public Map<String, String> getMdc() {
        return null;
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    @Override
    public void prepareForDeferredProcessing() {

    }

}
