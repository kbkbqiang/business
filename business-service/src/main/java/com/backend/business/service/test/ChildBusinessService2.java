package com.backend.business.service.test;

import org.springframework.stereotype.Service;

/**
 * @Description
 * @Author zq
 * @Date 2018/3/7
 */
@Service
public class ChildBusinessService2 extends AbsBusinessService {

    @Override
    public String test5() {
        return "我是第二次调用";
    }
}
