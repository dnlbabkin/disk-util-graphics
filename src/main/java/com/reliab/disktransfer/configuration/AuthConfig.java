package com.reliab.disktransfer.configuration;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
public class AuthConfig {

    @Bean
    public RestTemplate getTemplate(RestTemplateBuilder builder){
        return builder.build();
    }
}
