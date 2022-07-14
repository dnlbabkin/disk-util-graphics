package com.reliab.disktransfer.service;

import com.google.api.services.drive.model.File;
import com.reliab.disktransfer.configuration.properties.YandexProperties;
import com.reliab.disktransfer.component.GoogleAuth;
import com.reliab.disktransfer.configuration.properties.GoogleProperties;
import com.yandex.disk.rest.Credentials;
import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerException;
import com.yandex.disk.rest.exceptions.ServerIOException;
import com.yandex.disk.rest.json.Link;
import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService extends Task<List<File>> {

    private final int[] i = {0};

    private final YandexProperties yandexAuthProperties;
    private final GoogleProperties googleAuthProperties;
    private final GoogleService googleService;
    private final GoogleAuth googleAuth;


    @Override
    public List<File> call() {
        List<File> fileId = googleService.getFileList();
        int count = fileId.size();

        createFolder();
        countProgress(fileId, count);

        return fileId;
    }

    private void createFolder() {
        try {
            getRestClient().makeFolder(googleAuthProperties.getDownloadFolderName());
        } catch (ServerIOException | IOException e) {
            log.warn("Cannot create folder");
        }
    }

    private void countProgress(List<File> fileId, int count) {
        fileId.forEach(fileIds -> {
            this.showTransferProgress(fileIds);
            filesProcessing(fileIds);
            i[0]++;
            this.updateProgress(i[0], count);
        });
    }

    private void showTransferProgress(File file) {
        this.updateMessage("Переносится: " + file.getName());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            log.warn("Interrupted!");
            Thread.currentThread().interrupt();
        }
    }

    private void filesProcessing(File fileIds) {
        downloadFiles(fileIds);
        uploadFiles(fileIds, getRestClient());
        try {
            getRestClient().move(fileIds.getName(),
                    googleAuthProperties.getDownloadFolderName()
                            + "/" + fileIds.getName(), true);
        } catch (Exception e) {
            log.warn("Cannot move file to a directory");
        }
    }

    private void downloadFiles(File fileIds) {
        String directory = readDirectory();
        try(OutputStream outputStream = new FileOutputStream(directory + "/" + fileIds.getName())) {
            googleAuth.getDrive().files().get(fileIds.getId())
                    .executeMediaAndDownloadTo(outputStream);
        } catch (IOException e) {
            log.warn("Cannot download files");
        }
    }

    private void uploadFiles(File fileIds, RestClient restClient) {
        try {
            Link link = restClient.getUploadLink(fileIds.getName(), true);

            String directory = readDirectory();

            restClient.uploadFile(link, true,
                    new java.io.File(directory, fileIds.getName()), null);
        } catch (ServerException | IOException e) {
            log.warn("Cannot upload link or upload file");
        }
    }

    private RestClient getRestClient() {
        String token = readTokenFromFile();
        return new RestClient(new Credentials(null, token));
    }

    private String readTokenFromFile() {
        Path fileName = Path.of(yandexAuthProperties.getYandexTokensDirPath());
        try {
            return Files.readString(fileName);
        } catch (IOException e) {
            log.warn("Cannot read data from file", e);
        }
        return null;
    }

    private String readDirectory() {
        Path name = Path.of("src/main/resources/directory/");
        try {
            return Files.readString(name);
        } catch (IOException e) {
            log.warn("Cannot read directory");
        }

        return null;
    }
}
