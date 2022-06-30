package com.reliab.disktransfer.controller;

import com.reliab.disktransfer.service.YandexAuthService;
import com.yandex.disk.rest.json.DiskInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/callback")
@RequiredArgsConstructor
public class AccessTokenController {

    private final YandexAuthService yandexAuthService;

    @GetMapping("/yandex/token")
    public DiskInfo getAccessToken(@RequestParam String code) {
        return yandexAuthService.getToken(code);
    }
}
