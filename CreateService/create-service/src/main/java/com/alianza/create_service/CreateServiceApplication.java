package com.alianza.create_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.alianza.create_service"})
public class CreateServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CreateServiceApplication.class, args);
	}

}
