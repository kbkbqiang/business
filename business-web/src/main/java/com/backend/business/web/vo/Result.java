package com.backend.business.web.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class Result<T> implements Serializable {
    private static final long serialVersionUID = -3854093050720818340L;

    @ApiModelProperty(value = "0000(业务响应码)", required = true, example = "0000")
    private String respCode = "0000";
    @ApiModelProperty(value = "success(提示信息)", required = true, example = "success")
    private String message = "success";
    @ApiModelProperty(value = "业务数据", required = true)
    private T data;

    private Result(T data) {
        this.data = data;
    }

    @SuppressWarnings("unchecked")
    public static <T> Result<T> with(T data) {
        return new Result(data);
    }

}
