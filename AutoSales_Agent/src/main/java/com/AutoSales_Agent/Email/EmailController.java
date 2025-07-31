package com.AutoSales_Agent.Email;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
public class EmailController {

	private final EmailService emailService;
	
	@GetMapping("")
	public ResponseEntity<List<Email>> findEmails(
			@RequestParam(value = "projectId", required = false) Integer projectId,
			@RequestParam(value = "leadId", required = false) Integer leadId	
	){
		if(projectId!=null && leadId!=null) {
			return ResponseEntity.ok(this.emailService.findAllByProjectIdAndLeadId(projectId, leadId));
		}
		else if(projectId!=null) {
			return ResponseEntity.ok(this.emailService.findAllByProjectId(projectId));
		}
		else if(leadId!=null) {
			return ResponseEntity.ok(this.emailService.findAllByLeadId(leadId));
		}
		return ResponseEntity.ok(this.emailService.findAll());
	}
	
	@GetMapping("/lead/{leadId}/emails")
	public ResponseEntity<List<Email>> getEmailsByLead(
	        @PathVariable Integer leadId,
	        @RequestParam(required = false) Integer projectId
	) {
	    List<Email> emails = emailService.getEmailsByLead(leadId, projectId);
	    return ResponseEntity.ok(emails);
	}
	
	@PostMapping("")
	public ResponseEntity<Email> createEmail(@RequestBody EmailDto emailDto) {
		Email email=this.emailService.save(emailDto);
		return ResponseEntity.ok(email);
	}
}
