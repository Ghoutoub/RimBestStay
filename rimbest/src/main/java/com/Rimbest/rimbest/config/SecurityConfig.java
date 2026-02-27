package com.Rimbest.rimbest.config;

import com.Rimbest.rimbest.security.JwtAuthenticationFilter;
import com.Rimbest.rimbest.service.UserDetailsServiceImpl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // votre config CORS existante
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        // Lecture publique des hôtels et chambres
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/hotels/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/chambres/**").permitAll()
                        // Modifications nécessitent l'authentification
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/hotels/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/hotels/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/hotels/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/chambres/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/chambres/**").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/chambres/**").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/test/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()
                        .requestMatchers("/error").permitAll() // Évite 403 sur page d'erreur Spring
                        .anyRequest().authenticated())
                .authenticationProvider(authenticationProvider()) // votre DaoAuthenticationProvider
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*")); // Autoriser toutes les origines
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}