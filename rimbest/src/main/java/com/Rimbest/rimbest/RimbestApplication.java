package com.Rimbest.rimbest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RimbestApplication {

	public static void main(String[] args) {
		SpringApplication.run(RimbestApplication.class, args);
	}

}
