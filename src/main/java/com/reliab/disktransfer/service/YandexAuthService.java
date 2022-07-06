package com.reliab.disktransfer.service;

import com.reliab.disktransfer.configuration.properties.YandexProperties;
import com.reliab.disktransfer.dto.Token;
import com.reliab.disktransfer.exception.TokenProcessingException;
import com.reliab.disktransfer.ui.JavafxApplication;
import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.ResourcesArgs;
import com.yandex.disk.rest.RestClient;
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
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Slf4j
@Service
public class YandexAuthService {

    private final RestTemplate template;
    private final YandexProperties yandexAuthProperties;

    private static final ResourcesArgs RESOURCES_ARGS = new ResourcesArgs.Builder().build();

    private RestClient restClient;

    public YandexAuthService(@Qualifier("yandexRestTemplate") RestTemplate template,
                             YandexProperties yandexAuthProperties) {
        this.template = template;
        this.yandexAuthProperties = yandexAuthProperties;
    }

    private String getTokenFromFile() {
        Path fileName = Path.of(yandexAuthProperties.getYandexTokensDirPath());
        try {
            return Files.readString(fileName);
        } catch (IOException e) {
            log.warn("Cannot read data from file");
        }

        return null;
    }

    private RestClient getRestClient() {
        String token = getTokenFromFile();
        Credentials credentials = new Credentials(null, token);
        restClient = new RestClient(credentials);
        return restClient;
    }

    private void logging() {
        restClient = getRestClient();
        try {
            log.info(String.valueOf(restClient.getFlatResourceList(RESOURCES_ARGS)));
        } catch (Exception e) {
            log.warn("Cannot create client");
        }
    }

    private void tokenProcessing(String code) {
        String token = exchangeCodeToToken(code)
                .map(Token::getAccessToken)
                .orElseThrow(() -> new TokenProcessingException("Not token")
                );
        saveToFile(token);
    }

    private void saveToFile(String token) {
        try (PrintWriter writer = new PrintWriter(yandexAuthProperties
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
        body.add("client_id", yandexAuthProperties.getClientId());
        body.add("client_secret", yandexAuthProperties.getClientSecret());

        return body;
    }

    private Optional<Token> exchangeCodeToToken(String code) {
        HttpHeaders headers = createHeaders();
        MultiValueMap<String, String> body = createBody(code);
        ResponseEntity<Token> responseEntity = template.postForEntity(
                yandexAuthProperties.getTokenUrl(),
                new HttpEntity<>(body, headers),
                Token.class
        );
        return Optional.ofNullable(responseEntity.getBody());
    }

    public void browse() {
        JavafxApplication javafxApplication = new JavafxApplication();
        javafxApplication.browser(yandexAuthProperties.getRedirectUri());
    }

    public void handleToken(String code) {
        tokenProcessing(code);
        logging();
    }
}
