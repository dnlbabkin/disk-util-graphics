package com.reliab.disktransfer.service;

import com.google.api.services.drive.model.File;
import com.reliab.disktransfer.configuration.properties.YandexProperties;
import com.reliab.disktransfer.component.GoogleAuth;
import com.reliab.disktransfer.configuration.properties.GoogleProperties;
import com.reliab.disktransfer.ui.controller.DirectoryChooserController;
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
    private final DirectoryChooserController directoryChooserController;


    @Override
    public List<File> call() {
        List<File> fileId = googleService.getFileList();
        int count = fileId.size();

        createFolder();
        countProgress(fileId, count);

        return fileId;
    }

    public void filesProcessing(File fileIds)  {
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

    private void createFolder() {
        try {
            getRestClient().makeFolder(googleAuthProperties.getDownloadFolderName());
        } catch (ServerIOException | IOException e) {
            log.warn("Cannot create folder");
        }
    }

    private void countProgress(List<File> fileId, int count) {
        fileId.forEach(fileIds -> {
            boolean check = new java.io.File(directoryChooserController.getDirectory(), fileIds.getName()).exists();
            if(check){
                this.updateMessage(fileIds.getName() + " уже существует");
                log.warn("File already exist");
            } else {
                this.showTransferProgress(fileIds, count);
                filesProcessing(fileIds);
                i[0]++;
                this.updateProgress(i[0], count);
            }
        });
    }

    private void showTransferProgress(File file, int count) {
        this.updateMessage("Общее количество файлов: "
                + (count - i[0]) + "\n" + "Переносится: " + file.getName());
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            log.warn("Interrupted!");
            Thread.currentThread().interrupt();
        }
    }

    public void downloadFiles(File fileIds) {
        String directory = readDirectory();
        try(OutputStream outputStream = new FileOutputStream(directory + "/" + fileIds.getName())) {
            googleAuth.getDrive().files().get(fileIds.getId()).executeMediaAndDownloadTo(outputStream);
        } catch (IOException e) {
            log.warn("Cannot download files");
        }
    }

    public void uploadFiles(File fileIds, RestClient restClient) {
        try {
            Link link = restClient.getUploadLink(fileIds.getName(), true);
            String directory = readDirectory();
            restClient.uploadFile(link, true,
                        new java.io.File(directory, fileIds.getName()), null);
        } catch (ServerException | IOException e) {
            log.warn("Cannot upload link");
        }
    }

    public RestClient getRestClient() {
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


