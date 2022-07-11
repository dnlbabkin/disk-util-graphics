package com.reliab.disktransfer.service.impl;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.reliab.disktransfer.component.GoogleAuth;
import com.reliab.disktransfer.service.GoogleService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoogleServiceImpl implements GoogleService {

    private final GoogleAuth googleAuth;

    @SneakyThrows(IOException.class)
    @Override
    public List<File> getFileList() {
        FileList result = googleAuth.getDrive().files().list()
                    .setFields("nextPageToken, files(id, name)")
                    .execute();
        return result.getFiles();
    }
}