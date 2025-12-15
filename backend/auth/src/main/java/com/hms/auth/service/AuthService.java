package com.hms.auth.service;

import com.hms.auth.dto.AuthResponse;
import com.hms.auth.dto.LoginRequest;
import com.hms.auth.dto.SignupRequest;
import com.hms.auth.exception.ApiException;
import com.hms.auth.model.Role;
import com.hms.auth.model.User;
import com.hms.auth.repository.RoleRepository;
import com.hms.auth.repository.UserRepository;
import com.hms.auth.security.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;



    // 👇 abhi sirf signup handle karega
    @Transactional
    public void registerUser(SignupRequest signupRequest) {

        logger.info("Register attempt: {}", signupRequest.getUsername());

        if (userRepository.existsByUsername(signupRequest.getUsername())) {
            logger.warn("Username exists: {}", signupRequest.getUsername());
            throw new ApiException("Username already exists","AUTH_USER_EXISTS");
            //return "Error: Username already exists!";
        }

//        User user = new User();
//        user.setUsername(signupRequest.getUsername());
//        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));

        Role role = roleRepository.findByName(signupRequest.getRole())
                .orElseThrow(() -> new ApiException("Role not found: " + signupRequest.getRole(),
                                                        "AUTH_ROLE_NOT_FOUND"));

        User user = User.builder().username(signupRequest.getUsername())
                        .password(passwordEncoder.encode(signupRequest.getPassword()))
                        .build();
        user.setRoles(Set.of(role));
        userRepository.save(user);

        logger.info("User registered: {}", signupRequest.getUsername());
        //return "User registered successfully!";
    }

    // LogIn
    public AuthResponse login(LoginRequest loginRequest)
    {
        logger.info("Login attempt: {}", loginRequest.getUsername());

       // Authentication authentication = authenticationManager.authenticate(
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        User user = userRepository.findByUsernameWithRoles(loginRequest.getUsername())
                    .orElseThrow( ()-> new ApiException("User not found", "AUTH_USER_NOT_FOUND"));

        Set<String> roles = user.getRoles().stream()
                                .map(Role::getName)
                                .collect(Collectors.toSet());

        // JWT generate karo
        String token = jwtUtil.generateToken(user.getUsername(), roles);

        logger.info("Login success: {} (roles={})", loginRequest.getUsername(), roles);
        return new AuthResponse(token, null, jwtUtil.getExpirationMs());
    }
}
