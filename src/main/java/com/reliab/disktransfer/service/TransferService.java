package com.reliab.disktransfer.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.reliab.disktransfer.configuration.properties.YandexProperties;
import com.reliab.disktransfer.googleauth.GoogleAuth;
import com.reliab.disktransfer.configuration.properties.GoogleProperties;
import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerException;
import com.yandex.disk.rest.exceptions.ServerIOException;
import com.yandex.disk.rest.json.Link;
import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService extends Task<List<File>> {

    private final int[] i = {0};

    private final YandexProperties yandexAuthProperties;
    private final GoogleProperties googleAuthProperties;

    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();


    @SneakyThrows(IOException.class)
    private List<File> getFileList() {
        FileList result = getDrive().files().list()
                .setFields("nextPageToken, files(id, name)")
                .execute();
        return result.getFiles();
    }

    @SneakyThrows({GeneralSecurityException.class, IOException.class})
    private Drive getDrive() {
        GoogleAuth googleAuth = new GoogleAuth(googleAuthProperties);
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(httpTransport,
                JSON_FACTORY, googleAuth.getCredentials(httpTransport))
                .setApplicationName(googleAuthProperties.getGoogleAppName())
                .build();
    }

    private String getTokenFromFile() {
        Path fileName = Path.of(yandexAuthProperties.getYandexTokensDirPath());
        try {
            return Files.readString(fileName);
        } catch (IOException e) {
            log.warn("Cannot read data from file", e);
        }
        return null;
    }

    private String getDirectory() {
        Path name = Path.of("src/main/resources/directory/");
        try {
            return Files.readString(name);
        } catch (IOException e) {
            log.warn("Cannot read directory");
        }

        return null;
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
            log.warn("Cannot upload link or upload file");
        }
    }

    private void createFolder() {
        RestClient restClient = getRestClient();
        try {
            restClient.makeFolder(googleAuthProperties.getDownloadFolderName());
        } catch (ServerIOException | IOException e) {
            log.warn("Cannot create folder");
        }
    }

    private void downloadFiles(File fileIds) {
        String directory = getDirectory();
        try(OutputStream outputStream = new FileOutputStream(directory + "/" + fileIds.getName())) {
            getDrive().files().get(fileIds.getId())
                    .executeAndDownloadTo(outputStream);
        } catch (IOException e) {
            log.warn("Cannot download files");
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
            log.warn("Cannot move file to a directory");
        }
    }

    private void transferringFiles(File file) {
        this.updateMessage("Переносится: " + file.getName());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            log.warn("Interrupted!", e);
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public List<File> call() {
        List<File> fileId = getFileList();
        int count = fileId.size();

        createFolder();

        fileId.forEach(fileIds -> {
            this.transferringFiles(fileIds);
            fileOperations(fileIds);
            i[0]++;
            this.updateProgress(i[0], count);
        });

        return fileId;
    }
}
