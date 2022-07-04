package com.reliab.disktransfer.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


@Configuration
@RequiredArgsConstructor
public class RestTemplateConfig {

    private final RestTemplateBuilder builder;

    @Bean(name = "yandexRestTemplate")
    public RestTemplate yandexRestTemplate() {
        return builder.build();
    }
}
