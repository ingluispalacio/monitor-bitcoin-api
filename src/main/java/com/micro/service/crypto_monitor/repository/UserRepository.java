package com.micro.service.crypto_monitor.repository;

import com.micro.service.crypto_monitor.model.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository extends ReactiveCrudRepository<User, UUID> {

    Mono<User> findByUsername(String username);
    Mono<User> findByEmail(String email);
}
