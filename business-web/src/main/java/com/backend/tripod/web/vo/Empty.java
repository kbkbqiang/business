package com.backend.tripod.web.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

import java.io.Serializable;

/**
 * Created by zhangwanli on 2017/8/26.
 */
@Getter
@ApiModel("Result")
public class Empty implements Serializable {
    private static final long serialVersionUID = 3754821462070853628L;

    @ApiModelProperty(value = "0000(业务响应码)", required = true, example = "0000")
    private String respCode = "0000";
    @ApiModelProperty(value = "success(提示信息)", required = true, example = "success")
    private String message = "success";

    private Empty() {

    }

    public static Empty getInstance() {
        return new Empty();
    }

}
