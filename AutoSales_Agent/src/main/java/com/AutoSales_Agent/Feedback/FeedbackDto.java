package com.AutoSales_Agent.Feedback;

import java.time.LocalDateTime;

import com.AutoSales_Agent.Email.Email;
import com.AutoSales_Agent.Lead.Lead;
import com.AutoSales_Agent.Project.Project;

import lombok.Data;

@Data
public class FeedbackDto {

	private Integer projectId;
	private Integer leadId;
	private Integer emailId;
	private String responseSummary;
	private String responseType; 
	private String originalText;
	private LocalDateTime createdAt;
	private String leadName;
	private String projectName;

	// Entity → Dto (응답용)
	public static FeedbackDto fromEntity(Feedback feedback) {
		FeedbackDto dto = new FeedbackDto();
		dto.setProjectId(feedback.getProject().getId());
		dto.setLeadId(feedback.getLead().getId());
		dto.setEmailId(feedback.getMail().getId());
		dto.setResponseSummary(feedback.getResponseSummary());
		dto.setResponseType(feedback.getResponsetype());
		dto.setOriginalText(feedback.getOriginalText());
		dto.setCreatedAt(feedback.getCreatedAt());
		dto.setLeadName(feedback.getLead().getName());
		dto.setProjectName(feedback.getProject().getName());
		return dto;
	}

	// Dto → Entity (저장용)
	public static Feedback toEntity(FeedbackDto dto, Project project, Lead lead, Email email) {
		Feedback feedback = new Feedback();
		feedback.setProject(project);
		feedback.setLead(lead);
		feedback.setMail(email);
		feedback.setResponseSummary(dto.getResponseSummary());
		feedback.setOriginalText(dto.getOriginalText());
		feedback.setResponsetype(dto.getResponseType());
		return feedback;
	}
}
