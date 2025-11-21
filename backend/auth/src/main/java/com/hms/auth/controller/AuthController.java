package com.hms.auth.controller;

import com.hms.auth.DTO.AuthResponse;
import com.hms.auth.DTO.LoginRequest;
import com.hms.auth.DTO.SignupRequest;
import com.hms.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController
{
    @Autowired
    private AuthService authService;

    // 🔹 Signup API
    @PostMapping("/signup")
    public String signup(@RequestBody SignupRequest signupRequest) {
        System.out.println("********* AuthController Signup ********* ");
        return authService.registerUser(signupRequest);
    }

    //  yahan login endpoint bhi add karenge
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest loginRequest)
    {
        return authService.login(loginRequest);
    }

}
