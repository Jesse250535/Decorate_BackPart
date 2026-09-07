package com.example.decoratebackservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.decoratebackservice.mapper")
public class DecorateBackServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DecorateBackServiceApplication.class, args);
    }

}
