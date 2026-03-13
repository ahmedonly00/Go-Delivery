package com.goDelivery.goDelivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.goDelivery.goDelivery.shared.config.AsyncConfig;

@SpringBootApplication
@EnableAsync
@EnableScheduling
@EntityScan(basePackages = "com.goDelivery.goDelivery")
@Import(AsyncConfig.class)
public class GoDeliveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(GoDeliveryApplication.class, args);
    }
}
