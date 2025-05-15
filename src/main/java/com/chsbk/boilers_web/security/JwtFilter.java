package com.chsbk.boilers_web.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwt;

    public JwtFilter(JwtUtil jwt) {
        this.jwt = jwt;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         chain) throws ServletException, IOException {

        extractToken(request)
                .flatMap(this::safeUserId)
                .ifPresent(uid -> authenticate(uid, request));

        chain.doFilter(request, response);
    }

    private Optional<String> extractToken(HttpServletRequest request) {

        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return Optional.of(bearer.substring(7));
        }

        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("AuthToken".equals(c.getName())) {
                    return Optional.ofNullable(c.getValue());
                }
            }
        }
        return Optional.empty();
    }

    private Optional<Long> safeUserId(String token) {
        try {
            return Optional.of(jwt.getUserId(token));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    private void authenticate(Long uid, HttpServletRequest request) {
        var principal = org.springframework.security.core.userdetails.User
                .withUsername(String.valueOf(uid))
                .password("")
                .authorities(Collections.emptyList())
                .build();

        var auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
