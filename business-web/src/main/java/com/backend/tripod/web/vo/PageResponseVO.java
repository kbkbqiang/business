package com.backend.tripod.web.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@ApiModel("分页数据")
public class PageResponseVO<T> {
    @ApiModelProperty(value = "总记录数", required = true)
    private Integer totalCount = 0;
    @ApiModelProperty(value = "总页数", required = true)
    private Integer totalPage = 0;
    @ApiModelProperty(value = "当前页列表数据", required = true)
    private List<T> items = new ArrayList<>();
}
