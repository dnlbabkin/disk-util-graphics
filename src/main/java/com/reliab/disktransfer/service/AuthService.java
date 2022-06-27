package com.reliab.disktransfer.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.reliab.disktransfer.configuration.RestTemplateConfig;
import com.reliab.disktransfer.configuration.properties.YandexProperties;
import com.reliab.disktransfer.dto.Token;
import com.reliab.disktransfer.googleauth.GoogleAuth;
import com.reliab.disktransfer.configuration.properties.GoogleProperties;
import com.reliab.disktransfer.ui.JavafxApplication;
import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.ResourcesArgs;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.json.DiskInfo;
import com.yandex.disk.rest.json.Link;
import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService extends Task<List<File>> {

    private final RestTemplateConfig template;
    private final YandexProperties yandexAuthProperties;
    private final GoogleProperties googleAuthProperties;

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
        return template.getTemplate()
                .postForEntity(yandexAuthProperties.getTokenUrl(), request, Token.class);
    }

    @SneakyThrows
    private Drive getDrive() {
        GoogleAuth googleAuth = new GoogleAuth(googleAuthProperties);
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(httpTransport,
                JSON_FACTORY, googleAuth.getCredentials(httpTransport))
                .setApplicationName(googleAuthProperties.getApplicationName())
                .build();
    }

    @SneakyThrows
    private void saveToFile(String token) {
        PrintWriter writer = new PrintWriter(yandexAuthProperties.getYandexTokensDirPath());
        writer.println(token);
        writer.close();
    }

    @SneakyThrows
    private String getTokenFromFile() {
        Path fileName = Path.of(yandexAuthProperties.getYandexTokensDirPath());
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
    private void createFolder() {
        RestClient restClient = getRestClient();
        restClient.makeFolder(googleAuthProperties.getDownloadFolderName());
    }

    @SneakyThrows
    private void downloadFiles(File fileIds) {
        String directory = getDirectory();
        OutputStream outputStream = new FileOutputStream(directory +
                "/" + fileIds.getName());
        getDrive().files().get(fileIds.getId())
                .executeAndDownloadTo(outputStream);
    }

    @SneakyThrows
    private void fileOperations(File fileIds) {
        downloadFiles(fileIds);
        RestClient restClient = getRestClient();
        uploadFiles(fileIds, restClient);
        restClient.move(fileIds.getName(),
                googleAuthProperties.getDownloadFolderName()
                        + "/" + fileIds.getName(), true);
    }

    @SneakyThrows
    private RestClient getClient() {
        RestClient restClient = getRestClient();
        ResourcesArgs builder = new ResourcesArgs.Builder().build();
        log.info(String.valueOf(restClient.getFlatResourceList(builder)));
        return restClient;
    }

    private void setBody(String code) {
        HttpHeaders headers = getHttpHeaders();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("client_id", yandexAuthProperties.getClientId());
        body.add("client_secret", yandexAuthProperties.getClientSecret());

        saveTokenToFile(headers, body);
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private void saveTokenToFile(HttpHeaders headers, MultiValueMap<String, String> body) {
        ResponseEntity<Token> response = getTokenResponseEntity(headers, body);

        String token = Objects.requireNonNull(response.getBody()).getAccessToken();
        saveToFile(token);
    }

    @SneakyThrows
    public DiskInfo getToken(String code) {
        setBody(code);

        RestClient restClient = getClient();

        return restClient.getDiskInfo();
    }

    @SneakyThrows
    public void getFiles() {
        Drive service = getDrive();

        FileList result = service.files().list()
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        log.info("Files: ");
        for (File file : files) {
            log.info(file.getName(), file.getId());
        }
    }

    @SneakyThrows
    public void browse() {
        JavafxApplication javafxApplication = new JavafxApplication();
        javafxApplication.browser(yandexAuthProperties.getRedirectUri());
    }

    @SneakyThrows
    @Override
    public List<File> call() {
        List<File> fileId = getFileList();

        int count = fileId.size();
        int i = 0;

        createFolder();
        for(File fileIds : fileId) {
            fileOperations(fileIds);

            i++;
            this.updateProgress(i, count);
        }

        return fileId;
    }
}
