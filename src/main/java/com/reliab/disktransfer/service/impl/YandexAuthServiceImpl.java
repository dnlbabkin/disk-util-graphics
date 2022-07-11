package com.reliab.disktransfer.service.impl;

import com.reliab.disktransfer.configuration.properties.YandexProperties;
import com.reliab.disktransfer.dto.Token;
import com.reliab.disktransfer.exception.TokenProcessingException;
import com.reliab.disktransfer.service.YandexAuthService;
import com.reliab.disktransfer.ui.JavafxApplication;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Optional;

@Slf4j
@Service
public class YandexAuthServiceImpl implements YandexAuthService {

    private final RestTemplate template;
    private final YandexProperties yandexProperties;
    private final JavafxApplication javafxApplication;

    public YandexAuthServiceImpl(@Qualifier("yandexRestTemplate") RestTemplate template,
                                 YandexProperties yandexAuthProperties,
                                 JavafxApplication javafxApplication) {
        this.template = template;
        this.yandexProperties = yandexAuthProperties;
        this.javafxApplication = javafxApplication;
    }

    @Override
    public void browse() {
        javafxApplication.browser(yandexProperties.getRedirectUri());
    }

    @Override
    public void processCode(String code) {
        String token = exchangeCodeToToken(code)
                .map(Token::getAccessToken)
                .orElseThrow(() -> new TokenProcessingException("Not token"));
        saveToFile(token);
    }

    private Optional<Token> exchangeCodeToToken(String code) {
        HttpHeaders headers = createHeaders();
        MultiValueMap<String, String> body = createBody(code);
        ResponseEntity<Token> responseEntity = template.postForEntity(
                yandexProperties.getTokenUrl(),
                new HttpEntity<>(body, headers),
                Token.class
        );
        return Optional.ofNullable(responseEntity.getBody());
    }

    private void saveToFile(String token) {
        try (PrintWriter writer = new PrintWriter(yandexProperties
                .getYandexTokensDirPath())) {
            writer.println(token);
        } catch (FileNotFoundException e) {
            log.warn("Cannot find file");
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private MultiValueMap<String, String> createBody(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("client_id", yandexProperties.getClientId());
        body.add("client_secret", yandexProperties.getClientSecret());

        return body;
    }
}