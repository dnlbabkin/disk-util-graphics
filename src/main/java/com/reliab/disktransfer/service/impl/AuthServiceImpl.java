package com.reliab.disktransfer.service.impl;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.reliab.disktransfer.configuration.properties.GetTokenProperties;
import com.reliab.disktransfer.dto.Token;
import com.reliab.disktransfer.googleauth.GoogleAuth;
import com.reliab.disktransfer.configuration.properties.GoogleAuthProperties;
import com.reliab.disktransfer.service.AuthService;
import com.reliab.disktransfer.ui.controller.UIController;
import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.ResourcesArgs;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerException;
import com.yandex.disk.rest.exceptions.ServerIOException;
import com.yandex.disk.rest.exceptions.WrongMethodException;
import com.yandex.disk.rest.json.Link;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final RestTemplate template;
    private final GetTokenProperties properties;
    private final GoogleAuthProperties googleAuthProperties;
    public UIController controller;

    private static final String APPLICATION_NAME = "Drive transfer utility";
    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();


    private List<File> getFileList() throws IOException, GeneralSecurityException {
        FileList result = getDrive().files().list()
                .setFields("nextPageToken, files(id, name)")
                .execute();
        return result.getFiles();
    }

    private ResponseEntity<Token> getTokenResponseEntity(HttpHeaders headers, MultiValueMap<String, String> body) {
        HttpEntity<Map<String, String>> request = new HttpEntity(body, headers);
        ResponseEntity<Token> response = template.postForEntity(properties.getTokenUrl(), request, Token.class);
        return response;
    }

    private Drive getDrive() throws GeneralSecurityException, IOException {
        GoogleAuth googleAuth = new GoogleAuth(googleAuthProperties);
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, googleAuth.getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        return service;
    }

    private void saveTokenToFile(String token) throws IOException {
        PrintWriter writer = new PrintWriter(properties.getYandexTokensDirPath());
        writer.println(token);
        writer.close();
    }

    private String getTokenFromFile() throws IOException {
        Path fileName = Path.of(properties.getYandexTokensDirPath());
        return Files.readString(fileName);
    }

    private String getDirectory() throws IOException {
        Path name = Path.of("src/main/resources/directory/");
        return Files.readString(name);
    }

    private RestClient getRestClient() throws IOException {
        String token = getTokenFromFile();
        Credentials credentials = new Credentials(null, token);
        RestClient restClient = new RestClient(credentials);
        return restClient;
    }

    private void uploadFiles(File fileIds, RestClient restClient) throws IOException, ServerException {
        Link link = restClient.getUploadLink(fileIds.getName(), true);
        UIController controller = new UIController(null);
//        restClient.uploadFile(link, true,
//                new java.io.File("src/main/resources/tmp/", fileIds.getName()), null);
        String directory = getDirectory();
        restClient.uploadFile(link, true,
                new java.io.File(directory, fileIds.getName()), null);
    }

    private void downloadFiles(File fileIds) throws IOException, GeneralSecurityException {
//        OutputStream outputStream = new FileOutputStream("src/main/resources/tmp/" + fileIds.getName());
        String directory = getDirectory();
        OutputStream outputStream = new FileOutputStream(directory + fileIds.getName());
        getDrive().files().get(fileIds.getId()).executeAndDownloadTo(outputStream);
    }

    @Override
    public RestClient getToken(String code) throws IOException, ServerIOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("client_id", properties.getClientId());
        body.add("client_secret", properties.getClientSecrets());

        ResponseEntity<Token> response = getTokenResponseEntity(headers, body);

        String token = response.getBody().getAccessToken();
        saveTokenToFile(token);
        String tokenFromFile = getTokenFromFile();

        Credentials credentials = new Credentials(null, tokenFromFile);

        RestClient restClient = new RestClient(credentials);
        ResourcesArgs builder = new ResourcesArgs.Builder().build();
        System.out.println(restClient.getFlatResourceList(builder));

        return new RestClient(credentials);
    }

    @Override
    public com.google.api.services.drive.model.Drive getFiles() throws GeneralSecurityException, IOException {
        Drive service = getDrive();

        FileList result = service.files().list()
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        System.out.println("Files: ");
        for (File file : files) {
            System.out.printf("%s (%s)\n", file.getName(), file.getId());
        }
        return null;
    }

    @Override
    public void browse() {
        System.setProperty("java.awt.headless", "false");
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.browse(new URI(properties.getRedirectUri()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void fileTransfer() throws GeneralSecurityException, IOException, ServerException {
        List<File> fileId = getFileList();
        for(File fileIds : fileId) {
            downloadFiles(fileIds);

            RestClient restClient = getRestClient();

            uploadFiles(fileIds, restClient);

//            java.io.File deleteFile = new java.io.File("src/main/resources/tmp/", fileIds.getName());
            String directory = getDirectory();
            java.io.File deleteFile = new java.io.File(directory, fileIds.getName());
            deleteFile.delete();
        }
    }
}
