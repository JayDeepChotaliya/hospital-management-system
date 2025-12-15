package com.hms.auth.controller;

import com.hms.auth.dto.AuthResponse;
import com.hms.auth.dto.LoginRequest;
import com.hms.auth.dto.SignupRequest;
import com.hms.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
public class AuthController
{
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    // 🔹 Signup API
    @PostMapping(value = "/signup" , consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
        log.info("Signup request for {}", signupRequest.getUsername());
        authService.registerUser(signupRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "User registered successfully"));
    }

    //  yahan login endpoint bhi add karenge
    @PostMapping(value = "/login", consumes = "application/json" , produces = "application/json")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest)
    {
        log.info("Login attempt for {}", loginRequest.getUsername());
        AuthResponse authResponse = authService.login(loginRequest);
        log.info("AccessToken  {}", authResponse.getAccessToken());
        return ResponseEntity.ok(authResponse);
    }

}
