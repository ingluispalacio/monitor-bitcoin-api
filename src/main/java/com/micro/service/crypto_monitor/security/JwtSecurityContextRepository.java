package com.micro.service.crypto_monitor.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class JwtSecurityContextRepository implements ServerSecurityContextRepository {

    private final JwtAuthenticationManager authenticationManager;

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        
        // 🔍 LOG PARA VER TODOS LOS HEADERS
        log.info("========== 🔍 VERIFICANDO TOKEN ==========");
        log.info("URI: {}", exchange.getRequest().getURI());
        log.info("Método: {}", exchange.getRequest().getMethod());
        
        exchange.getRequest().getHeaders().forEach((key, value) -> 
            log.info("Header '{}': {}", key, value)
        );
        
        String token = extractToken(exchange.getRequest());
        
        if (token == null) {
            log.warn("❌ No se encontró token en la petición");
            return Mono.empty();
        }

        log.info("✅ Token encontrado: {}...", token.substring(0, Math.min(20, token.length())));
        
        return authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(null, token))
                .<SecurityContext>map(authentication -> {
                    log.info("✅ Autenticación exitosa para: {}", authentication.getPrincipal());
                    log.info("🔑 Authorities: {}", authentication.getAuthorities());
                    return new SecurityContextImpl(authentication);
                })
                .doOnError(error -> log.error("❌ Error en autenticación: {}", error.getMessage()))
                .onErrorResume(e -> {
                    log.error("❌ Error grave en autenticación", e);
                    return Mono.empty();
                });
    }

    private String extractToken(ServerHttpRequest request) {
        // 1️⃣ HEADER Authorization (para REST)
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            log.info("🔍 Token extraído de header Authorization");
            return authHeader.substring(7);
        }

        // 2️⃣ WEBSOCKET - Token en protocolo
        List<String> protocols = request.getHeaders().get("Sec-WebSocket-Protocol");
        if (protocols != null && !protocols.isEmpty()) {
            log.info("🔍 Protocolos WebSocket encontrados: {}", protocols);
            for (String protocol : protocols) {
                if (protocol != null && protocol.startsWith("Bearer_")) {
                    String token = protocol.substring(7);
                    log.info("✅ Token extraído de WebSocket protocol");
                    return token;
                }
            }
        }

        // 3️⃣ QUERY PARAM (útil para pruebas)
        String tokenParam = request.getQueryParams().getFirst("token");
        if (tokenParam != null) {
            log.info("🔍 Token extraído de query param (debug)");
            return tokenParam;
        }

        log.warn("❌ No se encontró token en ninguna parte");
        return null;
    }
}