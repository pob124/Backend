package com.AutoSales_Agent.Email;

import lombok.Data;

@Data
public class EmailDto {
	
	private Integer projectId;
	private Integer leadId;
	private String subject;
	private String body;
}
