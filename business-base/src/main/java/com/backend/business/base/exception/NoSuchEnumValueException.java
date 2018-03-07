package com.backend.business.base.exception;

public class NoSuchEnumValueException extends RuntimeException {
    private static final long serialVersionUID = 8221083196795251439L;

    public NoSuchEnumValueException(String message) {
        super(message);
    }

}
