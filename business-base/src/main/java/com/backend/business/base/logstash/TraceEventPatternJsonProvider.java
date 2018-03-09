package com.backend.business.base.logstash;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.fasterxml.jackson.core.JsonGenerator;
import net.logstash.logback.composite.AbstractJsonProvider;
import org.apache.commons.lang3.time.FastDateFormat;

import java.io.IOException;


public class TraceEventPatternJsonProvider extends AbstractJsonProvider<ILoggingEvent> {
    private final FastDateFormat format = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public void writeTo(JsonGenerator generator, ILoggingEvent iLoggingEvent) throws IOException {
        if (iLoggingEvent instanceof TraceMessageEvent) {
            TraceMessageEvent event = (TraceMessageEvent) iLoggingEvent;
            generator.writeFieldName("app");
            generator.writeString(event.getNameSpace());

            generator.writeFieldName("type");
            generator.writeString((event).getType());

            generator.writeFieldName("time");
            generator.writeString(format.format(event.getTimeStamp()));

            generator.writeFieldName("message");
            generator.writeObject(event.getRawMessage());

            generator.writeFieldName("traceId");
            generator.writeString(event.getTraceId());

            generator.writeFieldName("spanId");
            generator.writeString(event.getSpanId());
        }
    }

}
