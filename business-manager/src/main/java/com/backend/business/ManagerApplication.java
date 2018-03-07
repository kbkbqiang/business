package com.backend.business;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @Description
 * @Author zq
 * @Date 2018/3/7
 */
@SpringBootApplication
@MapperScan(basePackages = {"com.backend.business.dao.mapper"})
public class ManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ManagerApplication.class, args);
    }
}
