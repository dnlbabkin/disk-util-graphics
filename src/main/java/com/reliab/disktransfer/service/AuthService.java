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
import com.yandex.disk.rest.exceptions.ServerException;
import com.yandex.disk.rest.exceptions.ServerIOException;
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
import java.security.GeneralSecurityException;
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
    private static final ResourcesArgs RESOURCES_ARGS = new ResourcesArgs.Builder().build();


    @SneakyThrows(IOException.class)
    private List<File> getFileList() {
        FileList result = getDrive().files().list()
                .setFields("nextPageToken, files(id, name)")
                .execute();
        return result.getFiles();
    }

    private Drive getDrive() {
        GoogleAuth googleAuth = new GoogleAuth(googleAuthProperties);
        final NetHttpTransport httpTransport;
        try {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        } catch (GeneralSecurityException | IOException e) {
            throw new SecurityException(e);
        }
        return new Drive.Builder(httpTransport,
                JSON_FACTORY, googleAuth.getCredentials(httpTransport))
                .setApplicationName(googleAuthProperties.getApplicationName())
                .build();
    }

    private String getTokenFromFile() {
        Path fileName = Path.of(yandexAuthProperties.getYandexTokensDirPath());
        try {
            return Files.readString(fileName);
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    private String getDirectory() {
        Path name = Path.of("src/main/resources/directory/");
        try {
            return Files.readString(name);
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    private RestClient getRestClient() {
        String token = getTokenFromFile();
        Credentials credentials = new Credentials(null, token);
        return new RestClient(credentials);
    }

    private void uploadFiles(File fileIds, RestClient restClient) {
        try {
            Link link = restClient.getUploadLink(fileIds.getName(), true);

            String directory = getDirectory();

            restClient.uploadFile(link, true,
                    new java.io.File(directory, fileIds.getName()), null);
        } catch (ServerException | IOException e) {
            throw new SecurityException(e);
        }
    }

    private void createFolder() {
        RestClient restClient = getRestClient();
        try {
            restClient.makeFolder(googleAuthProperties.getDownloadFolderName());
        } catch (ServerIOException | IOException e) {
            throw new SecurityException(e);
        }
    }

    @SneakyThrows(FileNotFoundException.class)
    private void downloadFiles(File fileIds) {
        String directory = getDirectory();
        OutputStream outputStream = new FileOutputStream(directory +
                "/" + fileIds.getName());
        try {
            getDrive().files().get(fileIds.getId())
                    .executeAndDownloadTo(outputStream);
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    private void fileOperations(File fileIds) {
        downloadFiles(fileIds);
        RestClient restClient = getRestClient();
        uploadFiles(fileIds, restClient);
        try {
            restClient.move(fileIds.getName(),
                    googleAuthProperties.getDownloadFolderName()
                            + "/" + fileIds.getName(), true);
        } catch (Exception e) {
            throw new SecurityException(e);
        }
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

    private void setBody(String code) {
        HttpHeaders headers = getHttpHeaders();
        MultiValueMap<String, String> body = getStringMultiValueMap(code);

        saveTokenToFile(headers, body);
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
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
        return template.getTemplate()
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

    private void saveTokenToFile(HttpHeaders headers, MultiValueMap<String, String> body) {
        ResponseEntity<Token> response = getTokenResponseEntity(headers, body);

        String token = Objects.requireNonNull(response.getBody()).getAccessToken();
        saveToFile(token);
    }


    public DiskInfo getToken(String code) {
        setBody(code);

        RestClient restClient = getClient();

        try {
            return restClient.getDiskInfo();
        } catch (Exception e) {
            throw new SecurityException(e);
        }
    }

    public void getFileListFromGoogleDrive() {
        try {
            Drive service = getDrive();

            FileList result = service.files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .execute();

            List<File> files = result.getFiles();
            log.info("Files: ");
            for (File file : files) {
                log.info(file.getName(), file.getId());
            }
        } catch (IOException e) {
            throw new SecurityException(e);
        }
    }

    public void browse() {
        JavafxApplication javafxApplication = new JavafxApplication();
        javafxApplication.browser(yandexAuthProperties.getRedirectUri());
    }

    @Override
    public List<File> call() {
        List<File> fileId = getFileList();

        int count = fileId.size();
        int i = 0;

        createFolder();
        for (File fileIds : fileId) {
            fileOperations(fileIds);

            i++;
            this.updateProgress(i, count);
        }

        return fileId;
    }
}
