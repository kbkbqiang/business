package com.backend.business.exception;

/**
 * Created by zhangwanli on 2017/11/18.
 */
public class NoSuchEnumValueException extends RuntimeException {
    private static final long serialVersionUID = 8221083196795251439L;

    public NoSuchEnumValueException(String message) {
        super(message);
    }

}
