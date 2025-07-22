package com.AutoSales_Agent.Lead;

import com.AutoSales_Agent.Lead.Lead.Language;

import lombok.Data;

@Data
public class LeadDto {

	private String name;
    private String industry;
    private String contactEmail;
    private Language language;
}
