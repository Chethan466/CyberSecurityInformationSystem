package com.cybersecurity.cybersecurity_info_system.config;

import com.cybersecurity.cybersecurity_info_system.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final BCryptPasswordEncoder passwordEncoder;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService, BCryptPasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF: Enabled + Cookie-based (Thymeleaf + AJAX)
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            )

            // === AUTHORIZATION RULES (ORDER MATTERS!) ===
            .authorizeHttpRequests(auth -> auth
                // PUBLIC
                .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/register", "/login").permitAll()

                // ALL AUTHENTICATED USERS
                .requestMatchers("/dashboard", "/alerts", "/profile", "/profile/**").authenticated()

                // ADMIN ONLY
                .requestMatchers("/admin/**").hasRole("ADMIN")

                // === VIEWER READ-ONLY: LIST PAGES ONLY ===
                .requestMatchers("/incidents").hasAnyRole("ADMIN", "ANALYST", "VIEWER")
                .requestMatchers("/vulnerabilities").hasAnyRole("ADMIN", "ANALYST", "VIEWER")

                // === ANALYST + ADMIN: FULL CRUD (ALL SUB-PATHS) ===
                .requestMatchers("/incidents/**").hasAnyRole("ADMIN", "ANALYST")
                .requestMatchers("/vulnerabilities/**").hasAnyRole("ADMIN", "ANALYST")

                // ASSETS: ADMIN + ANALYST
                .requestMatchers("/assets", "/assets/**").hasAnyRole("ADMIN", "ANALYST")

                // Fallback: Require authentication
                .anyRequest().authenticated()
            )

            // === LOGIN ===
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard", true)
                .permitAll()
            )

            // === LOGOUT ===
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                .permitAll()
            );

        return http.build();
    }
}