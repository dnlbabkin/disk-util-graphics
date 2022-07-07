package com.reliab.disktransfer.controller;

import com.reliab.disktransfer.service.YandexAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/callback")
@RequiredArgsConstructor
public class CallbackHandlerController {

    public static final String RETURNED_MESSAGE = "Received verification code. You may now close this window.";

    private final YandexAuthService yandexAuthService;

    @GetMapping("/yandex/token")
    public String handleCallback(@RequestParam String code) {
        yandexAuthService.processCode(code);
        return RETURNED_MESSAGE;
    }
}
