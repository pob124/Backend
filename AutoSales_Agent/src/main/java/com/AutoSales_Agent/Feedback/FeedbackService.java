package com.AutoSales_Agent.Feedback;

import java.util.List;
import java.util.List;
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
	    return feedbackRepository.findFirstByMail_IdOrderByCreatedAtDesc(emailId);
	}
	
	public Feedback saveFeedback(FeedbackDto dto) {
	    Project project = projectRepository.findById(dto.getProjectId())
	        .orElseThrow(() -> new RuntimeException("Invalid projectId"));
	    Lead lead = leadRepository.findById(dto.getLeadId())
	        .orElseThrow(() -> new RuntimeException("Invalid leadId"));
	    Email email = null;
	    if (dto.getEmailId() != null) {
	        email = emailRepository.findById(dto.getEmailId())
	            .orElse(null); // 실패해도 진행
	    }

	    Feedback feedback = FeedbackDto.toEntity(dto, project, lead, email);
	    return feedbackRepository.save(feedback);
	}
	
	//최근 피드백 리스트 조회 (최신 50개)
	public List<Feedback> getRecentFeedbacks() {
	    return feedbackRepository.findTop50ByOrderByCreatedAtDesc();
	}
	
	//프로젝트별 피드백 리스트 조회
	public List<Feedback> getFeedbacksByProject(Integer projectId) {
	    return feedbackRepository.findByProject_IdOrderByCreatedAtDesc(projectId);
	}
	
	//리드별 피드백 리스트 조회
	public List<Feedback> getFeedbacksByLead(Integer leadId) {
	    return feedbackRepository.findByLead_IdOrderByCreatedAtDesc(leadId);
	}
}
