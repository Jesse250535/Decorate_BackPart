package com.example.decoratebackservice;

import  org.mybatis.spring.annotation.MapperScan;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.decoratebackservice")
@MapperScan("com.example.decoratebackservice.mapper")
class DecorateBackServiceApplicationTests {
    public static void main(String[] args) {
        SpringApplication.run(DecorateBackServiceApplication.class, args);
        SpringApplication.run(DecorateBackServiceApplicationTests.class, args);
    }

}
