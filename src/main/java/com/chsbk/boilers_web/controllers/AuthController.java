package com.chsbk.boilers_web.controllers;

import com.chsbk.boilers_web.utils.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestParam String username,
                                                     @RequestParam String password) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            String token = jwtTokenProvider.createToken(username, List.of("USER"));

            // формируем Set‑Cookie с SameSite
            ResponseCookie cookie = ResponseCookie.from("JWT", token)
                    .httpOnly(true)
                    .secure(true)          // ← вы же на HTTPS
                    .path("/")
                    .maxAge(Duration.ofDays(30))
                    .sameSite("Strict")     // Strict / Lax / None – как нужно
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    // JSON оставляем – вдруг фронту пригодится
                    .body(Map.of("token", token));

        } catch (AuthenticationException e) {
            throw new RuntimeException("Неверный логин или пароль");
        }
    }
}
