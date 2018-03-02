package com.backend.business.web.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

public class RequestUtil {

    public static JSONObject traceRequest(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, String> headers = new TreeMap<>();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        Enumeration<String> parameterNames = request.getParameterNames();
        Map<String, String> params = new TreeMap<>();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            params.put(paramName, request.getParameter(paramName));
        }
        JSONObject jsonObject = new JSONObject(2);
        jsonObject.put("headers", headers);
        jsonObject.put("params", params);
        return jsonObject;
    }


    public static String getUserRealIP(HttpServletRequest request) {
        String realIp = request.getHeader("X-Connecting-IP");
        if (StringUtils.isEmpty(realIp)) {
            realIp = request.getHeader("X-Real-IP");
        }
        if (StringUtils.isEmpty(realIp)) {
            realIp = request.getRemoteAddr();
        }
        return realIp;
    }

}
