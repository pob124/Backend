package com.AutoSales_Agent.Email;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.AutoSales_Agent.Lead.LeadService;
import com.AutoSales_Agent.Project.ProjectService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

	private final EmailRepository emailRepository;
	private final ProjectService projectService;
	private final LeadService leadService;
	
	public List<Email> findAll(){
		return this.emailRepository.findAll();
	}
	
	public  Email findById(Integer id) {
		return this.emailRepository.findById(id)
				.orElseThrow(()->new RuntimeException("메일을 찾을 수 없습니다"));
	}
	
	public List<Email> findAllByProjectIdAndLeadId(Integer projectId, Integer leadId) {
		return this.emailRepository.findAllByProjectIdAndLeadId(projectId, leadId);
	}
	
	public List<Email> findAllByProjectId(Integer projectId) {
		return this.emailRepository.findAllByProjectId(projectId);
	}
	
	public List<Email> findAllByLeadId(Integer leadId) {
		return this.emailRepository.findAllByLeadId(leadId);
	}
	
	public Email save(EmailDto emailDto) {
		Email email=new Email();
		email.setProject(this.projectService.findById(emailDto.getProjectId()));
		email.setLead(this.leadService.findById(emailDto.getLeadId()));
		email.setSubject(emailDto.getSubject());
		email.setBody(emailDto.getBody());
		return this.emailRepository.save(email);
	}
	
	public List<Email> getEmailsByLead(Integer leadId, Integer projectId) {
	    if (projectId != null) {
	        return emailRepository.findByLeadIdAndProjectId(leadId, projectId);
	    } else {
	        return emailRepository.findByLeadId(leadId);
	    }
	}
}
