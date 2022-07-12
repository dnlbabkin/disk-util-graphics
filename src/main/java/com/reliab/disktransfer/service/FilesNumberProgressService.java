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
public class FilesNumberProgressService extends Task<Void> {

    private final GoogleService googleService;

    @Override
    protected Void call() {
        List<File> files = googleService.getFileList();
        int count = files.size();
        for (int i = count - 1; i >= 0; i--) {
            this.updateMessage("Общее количество файлов на диске: " + i);
            this.updateProgress(i, count);
        }
        return null;
    }
}
