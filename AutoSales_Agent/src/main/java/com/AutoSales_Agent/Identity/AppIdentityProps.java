package com.AutoSales_Agent.Identity;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "app.identity")
public class AppIdentityProps {
    private String companyName;
    private String senderName;
    private String senderEmail;
}
