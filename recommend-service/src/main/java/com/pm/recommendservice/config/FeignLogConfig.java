package com.pm.recommendservice.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignLogConfig {
  @Bean
  feign.Logger.Level feignLoggerLevel() { return feign.Logger.Level.FULL; }
}