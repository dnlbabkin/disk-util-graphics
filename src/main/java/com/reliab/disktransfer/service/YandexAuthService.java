package com.reliab.disktransfer.service;

import com.reliab.disktransfer.configuration.RestTemplateConfig;
import com.reliab.disktransfer.configuration.properties.YandexProperties;
import com.reliab.disktransfer.dto.Token;
import com.reliab.disktransfer.ui.JavafxApplication;
import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.ResourcesArgs;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.json.DiskInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class YandexAuthService {

    private final RestTemplateConfig template;
    private final YandexProperties yandexAuthProperties;

    private static final ResourcesArgs RESOURCES_ARGS = new ResourcesArgs.Builder().build();


    private String getTokenFromFile() {
        Path fileName = Path.of(yandexAuthProperties.getYandexTokensDirPath());
        try {
            return Files.readString(fileName);
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    private RestClient getRestClient() {
        String token = getTokenFromFile();
        Credentials credentials = new Credentials(null, token);
        return new RestClient(credentials);
    }

    private RestClient getClient() {
        RestClient restClient = getRestClient();
        try {
            log.info(String.valueOf(restClient.getFlatResourceList(RESOURCES_ARGS)));
        } catch (Exception e) {
            throw new SecurityException(e);
        }

        return restClient;
    }

    private void saveTokenToFile(String code) {
        String token = Objects.requireNonNull(setRequestParameters(code).getBody()).getAccessToken();
        saveToFile(token);
    }

    private void saveToFile(String token) {
        try (PrintWriter writer = new PrintWriter(yandexAuthProperties
                .getYandexTokensDirPath())) {
            writer.println(token);
        } catch (FileNotFoundException e) {
            throw new SecurityException(e);
        }
    }

    private ResponseEntity<Token> getTokenResponseEntity(HttpHeaders headers,
                                                         MultiValueMap<String, String> body) {
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        return template.restTemplate()
                .postForEntity(yandexAuthProperties.getTokenUrl(), request, Token.class);
    }

    private MultiValueMap<String, String> getStringMultiValueMap(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("client_id", yandexAuthProperties.getClientId());
        body.add("client_secret", yandexAuthProperties.getClientSecret());

        return body;
    }

    private ResponseEntity<Token> setRequestParameters(String code) {
        MultiValueMap<String, String> body = getStringMultiValueMap(code);
        return getTokenResponseEntity(template.httpHeaders(), body);
    }

    public void browse() {
        JavafxApplication javafxApplication = new JavafxApplication();
        javafxApplication.browser(yandexAuthProperties.getRedirectUri());
    }

    public DiskInfo getToken(String code) {
        saveTokenToFile(code);

        RestClient restClient = getClient();

        try {
            return restClient.getDiskInfo();
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }
}
