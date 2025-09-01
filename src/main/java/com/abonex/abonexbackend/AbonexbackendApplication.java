package com.abonex.abonexbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AbonexbackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(AbonexbackendApplication.class, args);
	}

}
