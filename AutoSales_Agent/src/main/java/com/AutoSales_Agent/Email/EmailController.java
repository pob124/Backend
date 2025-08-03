package com.AutoSales_Agent.Email;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/emails")
public class EmailController {

	private final EmailService emailService;
	private final RedisTemplate<String, EmailDto> emailRedisTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;
    private final EmailDraftRedisService emailDraftRedisService;
    
 // 생성자에서 @Qualifier 사용
 	public EmailController(EmailService emailService, 
 	                      RedisTemplate<String, EmailDto> emailRedisTemplate,
 	                      @Qualifier("customStringRedisTemplate") RedisTemplate<String, String> stringRedisTemplate,
 	                     EmailDraftRedisService emailDraftRedisService) {
 		this.emailService = emailService;
 		this.emailRedisTemplate = emailRedisTemplate;
 		this.stringRedisTemplate = stringRedisTemplate;
 		this.emailDraftRedisService=emailDraftRedisService;
 	}
	
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
	
	// ✅ 개별 발송
    @PostMapping("/send/{uuid}")
    public ResponseEntity<String> sendSingleEmail(@PathVariable("uuid") String uuid) {
        EmailDto emailDto = emailRedisTemplate.opsForValue().get("email:draft:" + uuid);

        if (emailDto == null) {
            return ResponseEntity.badRequest().body("❌ 초안 없음 (uuid: " + uuid + ")");
        }

        emailService.sendEmail(emailDto); // 실제 전송 + DB 저장
        emailRedisTemplate.delete("email:draft:" + uuid);
        
        String sessionId = emailDraftRedisService.findSessionIdByUuid(uuid);
        if (sessionId != null && emailDraftRedisService.countDrafts(sessionId) == 1) {
            emailDraftRedisService.deleteDraftsBySession(sessionId);
        }
        return ResponseEntity.ok("✅ 개별 메일 전송 완료 (uuid: " + uuid + ")");
    }
    
    // ✅ 일괄 발송
    @PostMapping("/send")
    public ResponseEntity<String> sendAllEmails(@RequestParam("sessionId") String sessionId) {
        List<String> draftIds = stringRedisTemplate.opsForList().range("email:draft:session:" + sessionId, 0, -1);

        if (draftIds == null || draftIds.isEmpty()) {
            return ResponseEntity.badRequest().body("❌ 해당 sessionId의 초안이 없음");
        }

        int successCount = 0;

        for (String uuid : draftIds) {
            EmailDto emailDto = emailRedisTemplate.opsForValue().get("email:draft:" + uuid);
            if (emailDto != null) {
                emailService.sendEmail(emailDto);
                emailRedisTemplate.delete("email:draft:" + uuid);
                successCount++;
            }
        }

        stringRedisTemplate.delete("email:draft:session:" + sessionId);

        return ResponseEntity.ok("✅ 총 " + successCount + "개 메일 전송 완료 (session: " + sessionId + ")");
    }
}
