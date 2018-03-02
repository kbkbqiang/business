package com.backend.business.web.vo;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class Error implements Serializable {
    private static final long serialVersionUID = -8962061099030440543L;

    protected String respCode;
    protected String message;

    public Error(String respCode, String message) {
        if (respCode == null || message == null) {
            throw new NullPointerException("neither of the respCode or message is null.");
        }
        this.respCode = respCode;
        this.message = message;
    }

}
