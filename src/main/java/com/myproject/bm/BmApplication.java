package com.myproject.bm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.myproject.bm.mapper")
public class BmApplication {

    public static void main(String[] args) {
        SpringApplication.run(BmApplication.class, args);
    }

}
