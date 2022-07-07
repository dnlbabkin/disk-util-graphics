package com.reliab.disktransfer.service;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.reliab.disktransfer.component.GoogleAuth;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleService {

    private final GoogleAuth googleAuth;

    @SneakyThrows(IOException.class)
    public List<File> getFileList() {
        FileList result = googleAuth.getDrive().files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
        return result.getFiles();
    }
}
