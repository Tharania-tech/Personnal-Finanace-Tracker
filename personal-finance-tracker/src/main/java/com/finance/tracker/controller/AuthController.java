package com.finance.tracker.controller;

import com.finance.tracker.dto.ApiResponse;
import com.finance.tracker.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody Map<String, String> req) {

        String username = req.get("username");
        String password = req.get("password");

        if ("admin".equals(username) && "1234".equals(password)) {

            String token = jwtUtil.generateToken(username);

            return ApiResponse.success("Login success",
                    Map.of(
                            "userId", username,
                            "token", token
                    ));
        }

        return ApiResponse.error("Invalid credentials");
    }
}