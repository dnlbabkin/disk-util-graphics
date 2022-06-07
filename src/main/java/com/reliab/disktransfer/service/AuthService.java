package com.reliab.disktransfer.service;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface AuthService {
//    RestClient getToken(String code) throws IOException, ServerIOException;
    com.google.api.services.drive.model.Drive getFiles() throws GeneralSecurityException, IOException;
//    void browse();
//    void fileTransfer() throws GeneralSecurityException, IOException, ServerException;
}
