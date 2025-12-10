package com.goDelivery.goDelivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.goDelivery.goDelivery.config.AsyncConfig;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@ComponentScan(basePackages = {
    "com.goDelivery.goDelivery",
    "com.goDelivery.goDelivery.config",
    "com.goDelivery.goDelivery.configSecurity",
    "com.goDelivery.goDelivery.service",
    "com.goDelivery.goDelivery.controller"
})
@Import(AsyncConfig.class)
public class MozDeliveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(MozDeliveryApplication.class, args);
    }
}
