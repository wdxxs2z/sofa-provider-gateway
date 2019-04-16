package com.wdxxs2z.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.wdxxs2z.gateway.*")
public class GatewayApplication {

    public static void main(String[] args) {

        ConfigurableApplicationContext run = SpringApplication.run(GatewayApplication.class, args);
    }

}
