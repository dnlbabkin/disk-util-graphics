package com.reliab.disktransfer.service;

import com.yandex.disk.rest.json.DiskInfo;
import lombok.SneakyThrows;


public interface AuthService {
    @SneakyThrows
    DiskInfo getToken(String code);
    @SneakyThrows
    void getFiles();
    void browse();
    @SneakyThrows
    void fileTransfer();
}
