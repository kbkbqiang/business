package com.backend.business.web.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
@ApiModel
public class PageRequestVO {
    private static final int MAX_PAGE_SIZE = 50;

    @ApiModelProperty(value = "第几页", required = true, example = "1")
    protected int pageNumber = 1;
    @ApiModelProperty(value = "每页条数", required = true, example = "1")
    protected int pageSize = 20;

    public void setPageNumber(int pageNumber) {
        if (pageNumber < 1) {
            return;
        }
        this.pageNumber = pageNumber;
    }

    public void setPageSize(int pageSize) {
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            return;
        }
        this.pageSize = pageSize;
    }

}
