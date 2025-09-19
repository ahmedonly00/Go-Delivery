package com.goDelivery.goDelivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = {
    "com.goDelivery.goDelivery",
    "com.goDelivery.goDelivery.config",
    "com.goDelivery.goDelivery.configSecurity"
})
public class GoDeliveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoDeliveryApplication.class, args);
    }
}
