package com.reliab.disktransfer.service;

import com.google.api.services.drive.model.File;
import javafx.concurrent.Task;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilesNumberProgressService extends Task<List<File>> {

    private final int[] i = {0};

    private final GoogleService googleService;
    private final TransferService transferService;

    @Override
    public List<File> call() {
        List<File> fileId = googleService.getFileList();
        int count = fileId.size();
        fileId.forEach(file ->  {
            updateFileProcess(count);
            transferService.filesProcessing(file);
            i[0]++;
            this.updateProgress(i[0], count);
        });
        return fileId;
    }

    private void updateFileProcess(int count) {
        this.updateMessage("Общее количество файлов: " + (count - i[0]));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            log.warn("Interrupted!");
            Thread.currentThread().interrupt();
        }
    }
}
