package com.reliab.disktransfer.googleauth;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.DriveScopes;
import com.reliab.disktransfer.configuration.properties.GoogleAuthProperties;
import lombok.RequiredArgsConstructor;

import java.awt.*;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
public class GoogleAuth implements AuthorizationCodeInstalledApp.Browser {

    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);

    private final GoogleAuthProperties properties;


    @Override
    public void browse(String url)  {
        extracted(url);
    }

    private void extracted(String url) {
        System.setProperty("java.awt.headless", "false");
        Desktop desktop = Desktop.getDesktop();
        try {
            desktop.browse(new URI(url));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream inputStream = GoogleAuth.class.getResourceAsStream(properties.getCredFilePath());
        if (inputStream == null) {
            throw new FileNotFoundException("Resourse not found: " + properties.getCredFilePath());
        }

        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(inputStream));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(properties.getGoogleTokensDirPath())))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();


        return new AuthorizationCodeInstalledApp(flow, receiver, this).authorize("user");
    }
}
