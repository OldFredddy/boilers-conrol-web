package com.chsbk.boilers_web.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static String loginPath = "/login";

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.withUsername("admin")
                .password(passwordEncoder().encode("178562198"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/login").permitAll()
                        .requestMatchers(loginPath).permitAll()
                        .requestMatchers("/devices/init", "/devices/update", "/files/download/", "/files/upload").permitAll()
                        .requestMatchers("/devices/**").permitAll()  // Разрешаем доступ к эндпоинту /getstates/{name}
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") // фейковая страница
                        .loginProcessingUrl(loginPath)
                        .defaultSuccessUrl("/devices", true) // Указываем страницу для перенаправления после успешного логина
                        .permitAll()
                )
                .logout(LogoutConfigurer::permitAll
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public static void setLoginPath(String newPath) {
        loginPath = newPath;
    }

    public static String getLoginPath() {
        return loginPath;
    }
}
