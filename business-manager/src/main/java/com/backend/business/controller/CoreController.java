package com.backend.business.controller;

import com.backend.business.service.weixin.CoreService;
import com.backend.business.utils.weixin.SignUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/core/")
@Api(tags = "微信相关接口", description = "/core")
public class CoreController {

    @Autowired
    private CoreService coreService;

    // 验证是否来自微信服务器的消息
    @ApiOperation("验证是否来自微信服务器的消息")
    @RequestMapping(value = "/signature", method = RequestMethod.GET)
    public String checkSignature(@RequestParam(name = "signature", required = false) String signature, @RequestParam(name = "nonce", required = false) String nonce,
                                 @RequestParam(name = "timestamp", required = false) String timestamp, @RequestParam(name = "echostr", required = false) String echostr) {
        // 通过检验signature对请求进行校验，若校验成功则原样返回echostr，表示接入成功，否则接入失败
        if (SignUtils.checkSignature(signature, timestamp, nonce)) {
            log.info("接入成功");
            return echostr;
        }
        log.error("接入失败");
        return "";
    }

    // 调用核心业务类接收消息、处理消息跟推送消息
    @ApiOperation("调用核心业务类接收消息、处理消息跟推送消息")
    @RequestMapping(value = "", method = RequestMethod.POST)
    public String post(HttpServletRequest req) {
        String respMessage = coreService.processRequest(req);
        return respMessage;
    }
}
