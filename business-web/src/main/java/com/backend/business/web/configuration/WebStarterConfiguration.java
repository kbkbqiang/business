package com.backend.business.web.configuration;

import com.backend.business.web.annotation.AuthIgnore;
import com.backend.business.web.convert.StringDateConvert;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdScalarSerializer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonProperties;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.filter.OrderedCharacterEncodingFilter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.filter.CharacterEncodingFilter;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;

@Configuration
@EnableSwagger2
@EnableConfigurationProperties({JacksonProperties.class})
public class WebStarterConfiguration {

    @Autowired
    private JacksonProperties jacksonProperties;

    @Bean
    public ObjectMapper jacksonObjectMapper(Jackson2ObjectMapperBuilder builder) {
        if (jacksonProperties.getDefaultPropertyInclusion() == null) {
            builder.serializationInclusion(JsonInclude.Include.NON_NULL);
        }
        builder.serializerByType(Long.class, new StdScalarSerializer<Long>(Long.class) {
            private static final long serialVersionUID = 4509301735278156105L;
            private static final long JS_INTEGER_MAX_PRECISION_VALUE = 999999999999999L;

            @Override
            public void serialize(Long value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                if (value > JS_INTEGER_MAX_PRECISION_VALUE) {
                    gen.writeString(value.toString());
                } else {
                    gen.writeNumber(value);
                }
            }
        });
        builder.serializerByType(BigDecimal.class, new StdScalarSerializer<BigDecimal>(BigDecimal.class) {
            private static final long serialVersionUID = 8876113622852558483L;

            @Override
            public void serialize(BigDecimal value, JsonGenerator gen, SerializerProvider provider) throws IOException {
	            if (null != value && value.scale() < 2) {
		            DecimalFormat df = new DecimalFormat("0.00");
		            gen.writeString(df.format(value));
	            } else {
		            gen.writeString(String.valueOf(value));
	            }
            }
        });
        builder.deserializerByType(Date.class, new StdScalarDeserializer<Date>(Date.class) {
            private static final long serialVersionUID = -1881315730504643020L;

            private StringDateConvert convert = new StringDateConvert();

            @Override
            public Date deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
                JsonToken t = p.getCurrentToken();
                if (t == JsonToken.VALUE_NUMBER_INT) {
                    return new Date(p.getLongValue());
                }
                if (t == JsonToken.VALUE_NULL) {
                    return getNullValue(ctx);
                }
                if (t == JsonToken.VALUE_STRING) {
                    Date date = this.convert.convert(p.getText());
                    if (date == null) {
                        date = _parseDate(p.getText().trim(), ctx);
                    }
                    return date;
                }
                // [databind#381]
                if (t == JsonToken.START_ARRAY && ctx.isEnabled(DeserializationFeature.UNWRAP_SINGLE_VALUE_ARRAYS)) {
                    p.nextToken();
                    final Date parsed = _parseDate(p, ctx);
                    t = p.nextToken();
                    if (t != JsonToken.END_ARRAY) {
                        handleMissingEndArrayForSingle(p, ctx);
                    }
                    return parsed;
                }
                return (Date) ctx.handleUnexpectedToken(_valueClass, p);
            }
        });
        return builder.createXmlMapper(false).build();
    }

    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new OrderedCharacterEncodingFilter();
        filter.setEncoding("utf-8");
        filter.setForceRequestEncoding(true);
        filter.setForceResponseEncoding(true);
        return filter;
    }

    @Controller
    @AuthIgnore
    @Api(hidden = true)
    public class IndexController implements ApplicationListener<ApplicationReadyEvent> {

        private String applicationName = "application";

        @ApiOperation(value = "首页", hidden = true)
        @RequestMapping("/")
        public String index(Model model) {
            model.addAttribute("applicationName", applicationName);
            return "index";
        }

        @Override
        public void onApplicationEvent(ApplicationReadyEvent event) {
            SpringApplication springApplication = event.getSpringApplication();
            Class<?> mainApplicationClass = springApplication.getMainApplicationClass();
            applicationName = mainApplicationClass.getSimpleName();
        }

    }

}


