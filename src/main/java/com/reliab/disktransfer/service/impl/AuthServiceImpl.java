package com.reliab.disktransfer.service.impl;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.reliab.disktransfer.configuration.AuthConfig;
import com.reliab.disktransfer.configuration.properties.GetTokenProperties;
import com.reliab.disktransfer.dto.Token;
import com.reliab.disktransfer.googleauth.GoogleAuth;
import com.reliab.disktransfer.configuration.properties.GoogleAuthProperties;
import com.reliab.disktransfer.service.AuthService;
import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.ResourcesArgs;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.json.DiskInfo;
import com.yandex.disk.rest.json.Link;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final RestTemplateBuilder builder;
    private final AuthConfig config;
    private final GetTokenProperties properties;
    private final GoogleAuthProperties googleAuthProperties;

    private static final String APPLICATION_NAME = "Drive transfer utility";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();


    @SneakyThrows
    private List<File> getFileList() {
        FileList result = getDrive().files().list()
                .setFields("nextPageToken, files(id, name)")
                .execute();
        return result.getFiles();
    }

    private ResponseEntity<Token> getTokenResponseEntity(HttpHeaders headers, MultiValueMap<String, String> body) {
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        return config.getTemplate(builder).postForEntity(properties.getTokenUrl(), request, Token.class);
    }

    @SneakyThrows
    private Drive getDrive() {
        GoogleAuth googleAuth = new GoogleAuth(googleAuthProperties);
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, googleAuth.getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    @SneakyThrows
    private void saveTokenToFile(String token) {
        PrintWriter writer = new PrintWriter(properties.getYandexTokensDirPath());
        writer.println(token);
        writer.close();
    }

    @SneakyThrows
    private String getTokenFromFile() {
        Path fileName = Path.of(properties.getYandexTokensDirPath());
        return Files.readString(fileName);
    }

    @SneakyThrows
    private String getDirectory() {
        Path name = Path.of("src/main/resources/directory/");
        return Files.readString(name);
    }

    private RestClient getRestClient() {
        String token = getTokenFromFile();
        Credentials credentials = new Credentials(null, token);
        return new RestClient(credentials);
    }

    @SneakyThrows
    private void uploadFiles(File fileIds, RestClient restClient) {
        Link link = restClient.getUploadLink(fileIds.getName(), true);
        String directory = getDirectory();
        restClient.uploadFile(link, true,
                new java.io.File(directory, fileIds.getName()), null);
    }

    @SneakyThrows
    private void downloadFiles(File fileIds) {
        String directory = getDirectory();
        OutputStream outputStream = new FileOutputStream(directory + "\\" + fileIds.getName());
        getDrive().files().get(fileIds.getId()).executeAndDownloadTo(outputStream);
    }

    @SneakyThrows
    @Override
    public DiskInfo getToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("client_id", properties.getClientId());
        body.add("client_secret", properties.getClientSecrets());

        ResponseEntity<Token> response = getTokenResponseEntity(headers, body);

        String token = Objects.requireNonNull(response.getBody()).getAccessToken();
        saveTokenToFile(token);

        RestClient restClient = getRestClient();
        ResourcesArgs builder = new ResourcesArgs.Builder().build();
        System.out.println(restClient.getFlatResourceList(builder));

        return restClient.getDiskInfo();
    }

    @SneakyThrows
    @Override
    public void getFiles() {
        Drive service = getDrive();

        FileList result = service.files().list()
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        System.out.println("Files: ");
        for (File file : files) {
            System.out.printf("%s (%s)\n", file.getName(), file.getId());
        }
    }

    @SneakyThrows
    @Override
    public void browse() {
        System.setProperty("java.awt.headless", "false");
        Desktop desktop = Desktop.getDesktop();
        desktop.browse(new URI(properties.getRedirectUri()));
    }

    @SneakyThrows
    @Override
    public void fileTransfer() {
        List<File> fileId = getFileList();
        for(File fileIds : fileId) {
            downloadFiles(fileIds);

            RestClient restClient = getRestClient();

            uploadFiles(fileIds, restClient);

            String directory = getDirectory();
            Thread.sleep(500);
            Files.deleteIfExists(Paths.get(directory + "\\", fileIds.getName()));
        }
    }
}
