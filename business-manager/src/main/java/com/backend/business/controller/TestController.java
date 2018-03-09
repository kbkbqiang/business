package com.backend.business.controller;

import com.backend.tripod.web.vo.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description
 * @Author zq
 * @Date 2018/3/9
 */
@RestController
@RequestMapping("/testController")
@Api(tags = "测试相关接口", description = "/testController")
@Slf4j
public class TestController {

    @GetMapping("/downloadTemplate")
    @ApiOperation("下载Excel模板")
    public Result downloadTemplate(HttpServletRequest request, HttpServletResponse response) {
        Map<String, Object> dataMap = new HashMap<>();
        log.info("测试相关接口=================");
        dataMap.put("downloadPath", "/templates/userPrizeTemplate.xlsx");
        return Result.with(dataMap);
    }
}
