package io.github.egd.prodigal.dynamic.rabbit.router.out;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableEurekaClient
@EnableDiscoveryClient
public class DynamicRabbitRouterOutStarter {

    public static void main(String[] args) {
        SpringApplication.run(DynamicRabbitRouterOutStarter.class, args);
    }

}
