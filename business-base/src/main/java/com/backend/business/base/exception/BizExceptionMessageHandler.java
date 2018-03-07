package com.backend.business.base.exception;


import com.backend.business.base.component.ReloadableMessageSource;
import com.backend.business.base.helper.ApplicationContextHelper;

public class BizExceptionMessageHandler {
    private static ReloadableMessageSource messageSource;

    public static String getMessage(String errorCode, Object... args) {
        if (messageSource == null) {
            messageSource = ApplicationContextHelper.getBean(ReloadableMessageSource.class);
        }
        return messageSource.getMessage(errorCode, args);
    }

}
