package com.rakeshgupta.cafebrew_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CafebrewBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CafebrewBackendApplication.class, args);
	}

}
