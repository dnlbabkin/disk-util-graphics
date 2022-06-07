package com.reliab.disktransfer.service;

import com.yandex.disk.rest.RestClient;
import com.yandex.disk.rest.exceptions.ServerException;
import com.yandex.disk.rest.exceptions.ServerIOException;
import com.yandex.disk.rest.exceptions.WrongMethodException;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface AuthService {
    RestClient getToken(String code) throws IOException, ServerIOException, ServerIOException;
    com.google.api.services.drive.model.Drive getFiles() throws GeneralSecurityException, IOException;
    void browse();
    void fileTransfer() throws GeneralSecurityException, IOException, ServerException, ServerException;
}
