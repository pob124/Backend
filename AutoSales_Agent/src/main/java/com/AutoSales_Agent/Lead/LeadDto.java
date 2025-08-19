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

    
    // Entity → Dto 변환 메서드
    public static LeadDto fromEntity(Lead lead) {
        LeadDto dto = new LeadDto();
        dto.setName(lead.getName());
        dto.setIndustry(lead.getIndustry());
        dto.setContactEmail(lead.getContactEmail());
        dto.setLanguage(lead.getLanguage());
        dto.setContactName(lead.getContactName());
        dto.setSize(lead.getSize());
        return dto;
    }
}
