package com.reliab.disktransfer.service;

import com.google.api.services.drive.model.File;
import com.yandex.disk.rest.exceptions.ServerIOException;
import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilesNumberProgressService extends Task<List<File>> {

    private final int[] i = {0};

    private final GoogleService googleService;

    @Override
    public List<File> call() {
        List<File> fileId = googleService.getFileList();
        int count = fileId.size();

//        fileId.forEach(file -> {
//            this.updateMessage("Общее количество файлов: " + count);
//            try {
//                Thread.sleep(500);
//            } catch (InterruptedException e) {
//                log.warn("Interrupted!");
//                Thread.currentThread().interrupt();
//            }
//            i[0]++;
//            this.updateProgress(i[0], count);
//        });
        for (int j = count -1; j >= 1; j--) {
            this.updateMessage("Общее количество файлов: " + j);
            i[0]++;
            this.updateProgress(i[0], count);
        }

        return fileId;
    }
}
