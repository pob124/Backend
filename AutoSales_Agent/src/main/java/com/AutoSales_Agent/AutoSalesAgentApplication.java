package com.AutoSales_Agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.AutoSales_Agent.Identity.AppIdentityProps;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties({ AppIdentityProps.class })
public class AutoSalesAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutoSalesAgentApplication.class, args);
	}

}
