package com.AutoSales_Agent.Lead;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class LeadDto {
	@JsonProperty("companyName")
	private String name;
	
    private String industry;
    private String contactEmail;
    
    @JsonProperty("language")
    private Language language;
    private String contactName;
    
    @JsonProperty("size")
    private String size;

}
