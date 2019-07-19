package com.wdxxs2z.gateway;

import com.alipay.sofa.rpc.config.ApplicationConfig;
import com.alipay.sofa.rpc.config.RegistryConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.wdxxs2z.gateway")
public class GatewayApplication {

    public static void main(String[] args) {

        System.setProperty("com.alipay.env", "shared");
        System.setProperty("com.alipay.instanceid", "000001");
        System.setProperty("com.antcloud.antvip.endpoint", "acvip-inner.sofa.ynet.com");

        ConfigurableApplicationContext run = SpringApplication.run(GatewayApplication.class, args);
    }

}
