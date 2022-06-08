package com.reliab.disktransfer.controller;

import com.reliab.disktransfer.service.AuthService;
import com.yandex.disk.rest.json.DiskInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/callback")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/token")
    public DiskInfo startPage(@RequestParam String code) {
        return authService.getToken(code);
    }
}
