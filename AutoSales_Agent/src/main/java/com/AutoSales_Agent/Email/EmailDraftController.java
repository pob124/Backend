package com.AutoSales_Agent.Email;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/emails")
public class EmailDraftController {
	
	private final EmailDraftRedisService emailDraftRedisService;
	
	@PostMapping("/drafts")
	public ResponseEntity<Map<String, Object>> storeDrafts(@RequestBody List<EmailDto> emails) {
		String sessionId = emailDraftRedisService.storeDrafts(emails);
		return ResponseEntity.ok(Map.of("sessionId", sessionId));
	}
	
	/*
	 * @GetMapping("/drafts") public ResponseEntity<List<EmailDraftWithUuid>>
	 * getDraftsBySession(@RequestParam("sessionId") String sessionId){ return
	 * ResponseEntity.ok(emailDraftRedisService.getDrafts(sessionId)); }
	 */
	
    @DeleteMapping("/draft/{uuid}")
    public ResponseEntity<Void> deleteDraft(@PathVariable String uuid) {
        emailDraftRedisService.deleteSingleDraft(uuid);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/drafts")
    public ResponseEntity<Void> deleteAllDraftsBySession(@RequestParam("sessionId") String sessionId){
        emailDraftRedisService.deleteDraftsBySession(sessionId);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/api/drafts")
    public ResponseEntity<List<EmailDraftWithUuid>> getDraftsBySession(
        @RequestParam("sessionId") String sessionId
    ) {
        // 취소된 이메일도 포함하여 조회
        return ResponseEntity.ok(emailDraftRedisService.getCancelledEmails(sessionId));
    }

}
