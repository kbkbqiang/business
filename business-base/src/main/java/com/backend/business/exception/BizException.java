package com.backend.business.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BizException extends RuntimeException {

    private static final long serialVersionUID = -5368015604984251379L;

    private String errorCode;
    private Object[] args;

    public BizException(String errorCode) {
        this(errorCode, (Object[]) null);
    }

    public BizException(String errorCode, Object... args) {
        super(BizExceptionMessageHandler.getMessage(errorCode, args));
        this.errorCode = errorCode;
        this.args = args;
    }

    public Object[] getArgs() {
        return args;
    }

}
