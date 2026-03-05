package com.micro.service.crypto_monitor.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.micro.service.crypto_monitor.security.JwtAuthenticationManager;
import com.micro.service.crypto_monitor.security.JwtSecurityContextRepository;
import com.micro.service.crypto_monitor.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableReactiveMethodSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    
    @Value("${application.request.mappings}")
    private String basePath;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        JwtAuthenticationManager authManager = new JwtAuthenticationManager(jwtUtil);
        JwtSecurityContextRepository contextRepository =
                new JwtSecurityContextRepository(authManager);

        return http
                .cors(cors -> {})
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .securityContextRepository(contextRepository)
                
                // 🔥 Manejo de excepciones con los métodos definidos
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler())
                )
                
                .authorizeExchange(exchange -> exchange
                        // 🔓 Swagger UI y recursos
                        .pathMatchers(
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/webjars/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/webjars/**"
                        ).permitAll()
                        
                        // 🔓 Endpoints públicos
                        .pathMatchers(basePath + "/auth/login").permitAll()
                        
                        // 🔒 Endpoints protegidos por roles
                        // .pathMatchers(basePath + "/users/**").hasRole("ADMIN")
                         .pathMatchers("/ws/**").permitAll() 
                        // .pathMatchers("/ws/admin").hasRole("ADMIN") 
                        // .pathMatchers("/ws/client").authenticated()
                        
                        // 🔒 Todo lo demás requiere autenticación
                        .anyExchange().authenticated()
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 🔥 Método que maneja 401 - No autenticado (elimina el popup)
    private ServerAuthenticationEntryPoint authenticationEntryPoint() {
        return (exchange, ex) -> {
            log.debug("Authentication error: {}", ex.getMessage());
            
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "No autenticado - Token JWT requerido");
            errorResponse.put("status", HttpStatus.UNAUTHORIZED.value());
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            errorResponse.put("path", exchange.getRequest().getPath().value());
            
            byte[] bytes;
            try {
                bytes = objectMapper.writeValueAsBytes(errorResponse);
            } catch (Exception e) {
                bytes = "{\"success\":false,\"message\":\"No autenticado\"}".getBytes();
            }
            
            return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
            );
        };
    }

    // 🔥 Método que maneja 403 - Autenticado pero sin permisos
    private ServerAccessDeniedHandler accessDeniedHandler() {
        return (exchange, ex) -> {
            log.debug("Access denied error: {}", ex.getMessage());
            
            exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
            exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Acceso denegado - No tienes permisos suficientes");
            errorResponse.put("status", HttpStatus.FORBIDDEN.value());
            errorResponse.put("timestamp", LocalDateTime.now().toString());
            errorResponse.put("path", exchange.getRequest().getPath().value());
            
            byte[] bytes;
            try {
                bytes = objectMapper.writeValueAsBytes(errorResponse);
            } catch (Exception e) {
                bytes = "{\"success\":false,\"message\":\"Acceso denegado\"}".getBytes();
            }
            
            return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(bytes))
            );
        };
    }
}