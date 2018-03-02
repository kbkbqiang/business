package com.backend.business.exception;


import com.backend.business.component.ReloadableMessageSource;
import com.backend.business.helper.ApplicationContextHelper;

public class BizExceptionMessageHandler {
    private static ReloadableMessageSource messageSource;

    public static String getMessage(String errorCode, Object... args) {
        if (messageSource == null) {
            messageSource = ApplicationContextHelper.getBean(ReloadableMessageSource.class);
        }
        return messageSource.getMessage(errorCode, args);
    }

}
