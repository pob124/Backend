package com.AutoSales_Agent.Email;

import java.util.List;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.AutoSales_Agent.Lead.Lead;
import com.AutoSales_Agent.Lead.LeadRepository;
import com.AutoSales_Agent.Lead.LeadService;
import com.AutoSales_Agent.Project.ProjectService;

import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

	private final EmailRepository emailRepository;
	private final ProjectService projectService;
	private final LeadService leadService;
	private final LeadRepository leadRepository;
	private final JavaMailSender mailSender;
	
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
	
	//email전송
	public void sendEmail(EmailDto dto) {
		String to;
		if(dto.getContactEmail() != null) {
			to = dto.getContactEmail();
	    } else {
	    	to = leadRepository.findById(dto.getLeadId())
	    			.map(Lead::getContactEmail)
	    			.orElseThrow(() -> new RuntimeException("리드의 이메일 주소를 찾을 수 없습니다."));
	    }
		
		try {
			MimeMessage message=mailSender.createMimeMessage();
			MimeMessageHelper helper=new MimeMessageHelper(message,"utf-8");
			
			helper.setTo(to);
			helper.setSubject(dto.getSubject());
	        helper.setText(dto.getBody(), true); // HTML 허용
	        helper.setFrom("sks02040204@gmail.com");
	        
	        mailSender.send(message);
	        System.out.println("✅ 메일 전송 성공: " + to);
	        save(dto);
		}catch(Exception e){
			 System.err.println("❌ 메일 전송 실패: " + e.getMessage());
	         throw new RuntimeException("메일 전송 실패");
		}
	}
}
