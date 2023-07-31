package io.github.egd.prodigal.sample.provider1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class Provider1Starter {

    public static void main(String[] args) {
        SpringApplication.run(Provider1Starter.class, args);
    }

}
