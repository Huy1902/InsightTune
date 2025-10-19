package com.pm.favoriteservice.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(basePackages = {"com.pm.favoriteservice"})
public class WebConfig {

    @Bean
    @LoadBalanced // nếu có Eureka
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
