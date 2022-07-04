package com.reliab.disktransfer.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "yandex")
public class YandexProperties {
    private String clientId;
    private String clientSecret;
    private String tokenUrl;
    private String redirectUri;
    private String yandexTokensDirPath;
}
