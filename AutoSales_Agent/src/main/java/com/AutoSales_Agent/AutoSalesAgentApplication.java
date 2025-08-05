package com.AutoSales_Agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AutoSalesAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutoSalesAgentApplication.class, args);
	}

}
