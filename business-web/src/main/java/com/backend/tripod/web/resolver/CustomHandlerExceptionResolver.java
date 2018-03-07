package com.backend.tripod.web.resolver;

import com.alibaba.fastjson.JSON;
import com.backend.business.component.ReloadableMessageSource;
import com.backend.business.exception.BizException;
import com.backend.tripod.web.vo.Error;
import org.apache.shiro.authz.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomHandlerExceptionResolver extends DefaultHandlerExceptionResolver {

    private final Logger logger = LoggerFactory.getLogger(CustomHandlerExceptionResolver.class);
    private static final String PARAMTER_ERROR = "BSC10001";    //请求参数错误
    private static final String INVALID_PARAMTER = "BSC10002";  //参数校验不通过
    private static final String REQUEST_WAY_ERROR = "BSC10003"; //请求方式错误
    private static final String UNAUTHORIZED = "BSC10004";  //无权限操作
    private static final String SYSTEM_ERROR = "SYS9999";   //系统错误

    private ReloadableMessageSource messageSource;

    public CustomHandlerExceptionResolver(ReloadableMessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE + 1000;
    }

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (shouldApplyTo(request, handler)) {
            prepareResponse(ex, response);
            ModelAndView result = doResolveException(request, response, handler, ex);
            if (result != null) {
                logException(ex, request);
            }
            return result;
        } else {
            return null;
        }
    }

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        ModelAndView modelAndView = super.doResolveException(request, response, handler, ex);
        if (modelAndView != null) {
            logger.error("request has an error: uri={},Content-Type={},error={}", request.getRequestURI(), request.getContentType(), ex.getMessage());
            return modelAndView;
        } else {
            if (ex instanceof BizException) {
                return handleBizException((BizException) ex, request, response, handler);
            }
            if (ex.getCause() instanceof BizException) {
                return handleBizException((BizException) ex.getCause(), request, response, handler);
            } else if (ex instanceof UnauthorizedException) {
                return handleUnauthorizedException((UnauthorizedException) ex, request, response, handler);
            } else {
                return handleSysException(ex, request, response, handler);
            }
        }
    }

    private ModelAndView handleBizException(BizException ex, HttpServletRequest request, HttpServletResponse response, Object handler) {
        String code = ex.getErrorCode();
        String message = ex.getMessage();
        logger.error("request has an error: uri={},content-type={},error={} {}", request.getRequestURI(), request.getContentType(), code, message);
        return writeRestResponse(code, message, request, response);
    }

    private ModelAndView handleUnauthorizedException(UnauthorizedException ex, HttpServletRequest request, HttpServletResponse response, Object handler) {
        String message = messageSource.getMessage(UNAUTHORIZED, request.getLocale(), ex.getMessage());
        logger.error("request has an error: uri={},content-type={},error={} {}, {}", request.getRequestURI(), request.getContentType(), UNAUTHORIZED, message, ex.getMessage());
        return writeRestResponse(UNAUTHORIZED, message, request, response);
    }

    private ModelAndView handleSysException(Exception ex, HttpServletRequest request, HttpServletResponse response, Object handler) {
        logger.error("request has an error: uri={},content-type={},error={} {}", request.getRequestURI(), request.getContentType(), SYSTEM_ERROR, ex);
        return writeRestResponse(SYSTEM_ERROR, messageSource.getMessage(SYSTEM_ERROR, request.getLocale()), request, response);
    }

    private ModelAndView writeRestResponse(String code, String message, HttpServletRequest request, HttpServletResponse response) {
        Error error = new Error(code, message);
        try {
            response.getWriter().print(JSON.toJSONString(error));
        } catch (IOException e) {
            logger.error("{}", e);
        }
        return new ModelAndView();
    }

    @Override
    protected ModelAndView handleMissingPathVariable(MissingPathVariableException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String message = messageSource.getMessage(PARAMTER_ERROR, request.getLocale(), ex.getMessage());
        return writeRestResponse(PARAMTER_ERROR, message, request, response);
    }

    @Override
    protected ModelAndView handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String message = messageSource.getMessage(PARAMTER_ERROR, request.getLocale(), ex.getMessage());
        return writeRestResponse(PARAMTER_ERROR, message, request, response);
    }

    @Override
    protected ModelAndView handleServletRequestBindingException(ServletRequestBindingException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String message = messageSource.getMessage(PARAMTER_ERROR, request.getLocale(), ex.getMessage());
        return writeRestResponse(PARAMTER_ERROR, message, request, response);
    }

    @Override
    protected ModelAndView handleConversionNotSupported(ConversionNotSupportedException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String message = messageSource.getMessage(PARAMTER_ERROR, request.getLocale(), ex.getMessage());
        return writeRestResponse(PARAMTER_ERROR, message, request, response);
    }

    @Override
    protected ModelAndView handleTypeMismatch(TypeMismatchException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String message = messageSource.getMessage(PARAMTER_ERROR, request.getLocale(), ex.getMessage());
        return writeRestResponse(PARAMTER_ERROR, message, request, response);
    }

    @Override
    protected ModelAndView handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String message = messageSource.getMessage(PARAMTER_ERROR, request.getLocale(), ex.getMessage().split(":")[0]);
        return writeRestResponse(PARAMTER_ERROR, message, request, response);
    }

    @Override
    protected ModelAndView handleHttpMessageNotWritable(HttpMessageNotWritableException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String message = messageSource.getMessage(PARAMTER_ERROR, request.getLocale(), ex.getMessage());
        return writeRestResponse(PARAMTER_ERROR, message, request, response);
    }

    @Override
    protected ModelAndView handleMissingServletRequestPartException(MissingServletRequestPartException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String message = messageSource.getMessage(PARAMTER_ERROR, request.getLocale(), ex.getMessage());
        return writeRestResponse(PARAMTER_ERROR, message, request, response);
    }

    @Override
    protected ModelAndView handleBindException(BindException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String message = messageSource.getMessage(PARAMTER_ERROR, request.getLocale(), ex.getMessage());
        return writeRestResponse(PARAMTER_ERROR, message, request, response);
    }

    @Override
    protected ModelAndView handleNoHandlerFoundException(NoHandlerFoundException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String message = messageSource.getMessage(PARAMTER_ERROR, request.getLocale(), ex.getMessage());
        return writeRestResponse(PARAMTER_ERROR, message, request, response);
    }

    @Override
    protected ModelAndView handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        ObjectError error = ex.getBindingResult().getAllErrors().get(0);
        String message = error.getDefaultMessage();
        //        String message = messageSource.getMessage(INVALID_PARAMTER, request.getLocale(), error.getDefaultMessage());
        return writeRestResponse(INVALID_PARAMTER, message, request, response);
    }

    @Override
    protected ModelAndView handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String message = messageSource.getMessage(REQUEST_WAY_ERROR, request.getLocale(), ex.getMessage());
        return writeRestResponse(REQUEST_WAY_ERROR, message, request, response);
    }

    @Override
    protected ModelAndView handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String message = messageSource.getMessage(REQUEST_WAY_ERROR, request.getLocale(), ex.getMessage());
        return writeRestResponse(REQUEST_WAY_ERROR, message, request, response);
    }

    @Override
    protected ModelAndView handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String message = messageSource.getMessage(REQUEST_WAY_ERROR, request.getLocale(), ex.getMessage());
        return writeRestResponse(REQUEST_WAY_ERROR, message, request, response);
    }

    @Override
    protected ModelAndView handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex, HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        return super.handleAsyncRequestTimeoutException(ex, request, response, handler);
    }

}
