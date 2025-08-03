package com.AutoSales_Agent.Email;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailDto {
	private static final long serialVersionUID = 1L;
	
	private Integer projectId;
	private Integer leadId;
	private String subject;
	private String body;
	private String contactEmail;
}
