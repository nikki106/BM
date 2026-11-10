package com.myproject.bm.controller;

import com.myproject.bm.common.Result;
import com.myproject.bm.entity.dto.LoginRequest;
import com.myproject.bm.entity.vo.LoginResult;
import com.myproject.bm.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginResult> login(@RequestBody LoginRequest request) {
        return Result.ok(authService.login(request.getUsername(), request.getPassword()));
    }
}
