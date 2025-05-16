package com.chsbk.boilers_web.controllers;


import com.chsbk.boilers_web.security.JwtUtil;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/auth")
@Validated
@RequiredArgsConstructor
public class AuthController {

    private static final String AUTH_COOKIE = "AuthToken";

    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder   passwordEncoder = new BCryptPasswordEncoder();

    record LoginRq(@NotBlank String login,
                   @NotBlank String password) { }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRq rq) {

        log.info("Auth request | login='{}'", rq.login());

        if (!(rq.login.equals("admin") && rq.password.equals("178562198"))) {
            throw new IllegalArgumentException("bad credentials");
        }

        String token = jwtUtil.generateToken(100L);

        ResponseCookie cookie = ResponseCookie
                .from(AUTH_COOKIE, token)
                .httpOnly(true)
                .path("/")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .build();
    }

    @GetMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = AUTH_COOKIE, required = false) String token) {

        safeUserId(token).ifPresent(
                id -> log.info("Logout       | userId={}", id));

        ResponseCookie expired = ResponseCookie
                .from(AUTH_COOKIE, "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.SET_COOKIE, expired.toString())
                .header(HttpHeaders.LOCATION, "/")
                .build();
    }



    private Optional<Long> safeUserId(String token) {

        if (token == null) {
            return Optional.empty();
        }

        try {
            return Optional.of(jwtUtil.getUserId(token));
        }
        catch (Exception ignored) {
            return Optional.empty();
        }
    }
}
