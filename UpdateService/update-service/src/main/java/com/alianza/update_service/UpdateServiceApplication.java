package com.alianza.update_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.alianza.update_service"})
public class UpdateServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UpdateServiceApplication.class, args);
	}

}
