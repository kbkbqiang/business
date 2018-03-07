package com.backend.tripod.web.advisor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class RequestBodyAdvisor extends RequestBodyAdviceAdapter {

    private static final Logger logger = LoggerFactory.getLogger(RequestBodyAdvisor.class);

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return methodParameter.getParameterAnnotation(RequestBody.class) != null;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        Method method = parameter.getMethod();
        String jsonBody;
        if (StringHttpMessageConverter.class.isAssignableFrom(converterType)) {
            jsonBody = body.toString();
        } else {
            jsonBody = JSON.toJSONString(body, SerializerFeature.UseSingleQuotes);
        }
        if (logger.isInfoEnabled()) {
            logger.info("json request<=========method:{}#{},body:{}", parameter.getContainingClass().getSimpleName(), method.getName(), jsonBody);
        }
        return super.afterBodyRead(body, inputMessage, parameter, targetType, converterType);
    }
}
