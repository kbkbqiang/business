package com.backend.business.service.test;

import org.springframework.stereotype.Service;

/**
 * @Description
 * @Author zq
 * @Date 2018/3/7
 */
@Service
public class ChildBusinessService extends AbsBusinessService {

    @Override
    public String test5() {
        setDefaultValue("我是第一次调用");
        return "123131231321";
    }
}
