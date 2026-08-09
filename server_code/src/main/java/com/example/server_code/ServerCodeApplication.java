package com.example.server_code;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.server_code.mapper")
public class ServerCodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServerCodeApplication.class, args);
    }

}
