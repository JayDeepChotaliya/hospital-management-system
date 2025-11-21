package com.hms.auth.service;

import com.hms.auth.DTO.AuthResponse;
import com.hms.auth.DTO.LoginRequest;
import com.hms.auth.DTO.SignupRequest;
import com.hms.auth.model.Role;
import com.hms.auth.model.User;
import com.hms.auth.repository.RoleRepository;
import com.hms.auth.repository.UserRepository;
import com.hms.auth.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;



    // 👇 abhi sirf signup handle karega
    public String   registerUser(SignupRequest signupRequest) {

        logger.info("Register attempt: {}", signupRequest.getUsername());
        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            logger.warn("Username exists: {}", signupRequest.getUsername());
            return "Error: Username already exists!";
        }

        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

        Role role = roleRepository.findByName(signupRequest.getRole())
                .orElseThrow(() -> new RuntimeException("Role not found: " + signupRequest.getRole()));

        user.setRoles(Collections.singleton(role));
        userRepository.save(user);

        logger.info("User registered: {}", signupRequest.getUsername());
        return "User registered successfully!";
    }

    // LogIn
    public AuthResponse login(LoginRequest loginRequest)
    {
        logger.info("Login attempt: {}", loginRequest.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        var user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow();
        Set<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());

        // JWT generate karo
        String token = jwtUtil.generateToken(loginRequest.getUsername());

        logger.info("Login success: {} (roles={})", loginRequest.getUsername(), roles);
        return new AuthResponse(token);
    }


}
