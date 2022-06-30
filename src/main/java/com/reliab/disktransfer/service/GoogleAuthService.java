package com.reliab.disktransfer.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.reliab.disktransfer.configuration.properties.GoogleProperties;
import com.reliab.disktransfer.googleauth.GoogleAuth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final GoogleProperties googleAuthProperties;

    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

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
}
