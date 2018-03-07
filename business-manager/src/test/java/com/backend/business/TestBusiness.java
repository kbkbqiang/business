package com.backend.business;

import com.backend.business.service.test.ChildBusinessService;
import com.backend.business.service.test.ChildBusinessService2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @Description
 * @Author zq
 * @Date 2018/3/7
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TestBusiness {

    @Autowired
    ChildBusinessService childBusinessService;

    @Autowired
    ChildBusinessService2 childBusinessService2;

    @Test
    public void test(){
        childBusinessService.test6();
        childBusinessService2.test6();
    }
}
