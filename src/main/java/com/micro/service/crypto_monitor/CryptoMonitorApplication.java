package com.micro.service.crypto_monitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CryptoMonitorApplication {

	public static void main(String[] args) {
		SpringApplication.run(CryptoMonitorApplication.class, args);
	}

}
