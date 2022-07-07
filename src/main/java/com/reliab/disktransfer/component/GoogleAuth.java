package com.reliab.disktransfer.component;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.reliab.disktransfer.configuration.properties.GoogleProperties;
import com.reliab.disktransfer.ui.JavafxApplication;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class GoogleAuth implements AuthorizationCodeInstalledApp.Browser {

    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

    private final GoogleProperties properties;

    @Override
    public void browse(String url) {
        JavafxApplication javafxApplication = new JavafxApplication();
        javafxApplication.browser(url);
    }

    @SneakyThrows({FileNotFoundException.class, IOException.class})
    public Credential getCredentials(final NetHttpTransport httpTransport) {
        InputStream inputStream = GoogleAuth.class.getResourceAsStream(properties.getCredFilePath());
        if (inputStream == null) {
            throw new FileNotFoundException("Resourse not found: " + properties.getCredFilePath());
        }
        GoogleClientSecrets clientSecrets =  GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(inputStream));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(properties.getGoogleTokensDirPath())))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();

        return new AuthorizationCodeInstalledApp(flow, receiver, this).authorize("user");
    }

    @SneakyThrows({GeneralSecurityException.class, IOException.class})
    public Drive getDrive() {
        final NetHttpTransport httpTransport;
        httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(httpTransport,
                JSON_FACTORY, getCredentials(httpTransport))
                .setApplicationName(properties.getGoogleAppName())
                .build();
    }
}
