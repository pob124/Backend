package com.AutoSales_Agent.Feedback;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.AutoSales_Agent.Email.Email;
import com.AutoSales_Agent.Email.EmailRepository;
import com.AutoSales_Agent.Lead.Lead;
import com.AutoSales_Agent.Lead.LeadRepository;
import com.AutoSales_Agent.Project.Project;
import com.AutoSales_Agent.Project.ProjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeedbackService {
	
	private final FeedbackRepository feedbackRepository;
	private final ProjectRepository projectRepository;
	private final LeadRepository leadRepository;
	private final EmailRepository emailRepository;

	public Optional<Feedback> getLatestFeedbackByEmailId(Integer emailId) {
	    return feedbackRepository.findFirstByEmailIdOrderByCreatedAtDesc(emailId);
	}
	
	public Feedback saveFeedback(FeedbackDto dto) {
	    Project project = projectRepository.findById(dto.getProjectId())
	        .orElseThrow(() -> new RuntimeException("Invalid projectId"));
	    Lead lead = leadRepository.findById(dto.getLeadId())
	        .orElseThrow(() -> new RuntimeException("Invalid leadId"));
	    Email email = emailRepository.findById(dto.getEmailId())
	        .orElseThrow(() -> new RuntimeException("Invalid emailId"));

	    Feedback feedback = FeedbackDto.toEntity(dto, project, lead, email);
	    return feedbackRepository.save(feedback);
	}
}
