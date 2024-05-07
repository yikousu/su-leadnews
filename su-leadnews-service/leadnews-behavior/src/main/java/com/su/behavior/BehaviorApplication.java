package com.su.behavior;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class BehaviorApplication {
    public static void main(String[] args) {
        SpringApplication.run(BehaviorApplication.class,args);
    }
}
