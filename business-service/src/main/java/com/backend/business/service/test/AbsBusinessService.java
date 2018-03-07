package com.backend.business.service.test;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @Description
 * @Author zq
 * @Date 2018/3/7
 */
@Getter
@Setter
public abstract class AbsBusinessService implements BusinessService {

    private String defaultValue = "AbsBusinessService";

    private void test1(){
        System.out.println("test1");
    }

    private void test2(){
        System.out.println("test2");
    }

    private void test3(){
        System.out.println("test3");
    }

    public abstract String test5();

    @Override
    public void test6(){
        test1();
        test2();
        test3();

        String flag = test5();// 查询大小=sheet size
        System.out.println(defaultValue + "===flag === " + flag);

    }
}
