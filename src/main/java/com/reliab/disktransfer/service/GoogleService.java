package com.reliab.disktransfer.service;

import com.google.api.services.drive.model.File;

import java.util.List;

public interface GoogleService {
    List<File> getFileList();
}
