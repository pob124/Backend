package com.AutoSales_Agent.Email;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/emails")
@RequiredArgsConstructor
public class EmailController {

	private final EmailService emailService;
	private final RedisTemplate<String, EmailDto> emailRedisTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;
    private final EmailDraftRedisService emailDraftRedisService;
    private final RestTemplate restTemplate;
    
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
	
	// 개별 발송
    @PostMapping("/send/{uuid}")
    public ResponseEntity<String> sendSingleEmail(@PathVariable("uuid") String uuid) {
        EmailDto emailDto = emailRedisTemplate.opsForValue().get("email:draft:" + uuid);

        if (emailDto == null) {
            return ResponseEntity.badRequest().body("❌ 초안 없음 (uuid: " + uuid + ")");
        }

        emailService.sendEmail(emailDto); // 실제 전송 + DB 저장
        emailRedisTemplate.delete("email:draft:" + uuid);
        emailRedisTemplate.delete("email:draft:sessionByUuid:" + uuid);
        
        String sessionId = emailDraftRedisService.findSessionIdByUuid(uuid);
        if (sessionId != null) {
            emailDraftRedisService.removeFromSession(sessionId, uuid);

            // 5) 세션 비었으면 삭제, 아니면 (선택) TTL 갱신
            if (emailDraftRedisService.countDrafts(sessionId) == 0) {
                emailDraftRedisService.deleteDraftsBySession(sessionId); // 내부에서 세션 키 삭제 포함
            } else {
                emailDraftRedisService.touchSessionTtl(sessionId); // 선택
            }
        }
        return ResponseEntity.ok("✅ 개별 메일 전송 완료 (uuid: " + uuid + ")");
    }
    
    
    
    //발송 취소 기능 (내용 업데이트 방식)
    @PostMapping("/cancel/{uuid}")
    public ResponseEntity<String> cancelEmail(@PathVariable("uuid") String uuid, @RequestBody Map<String, String> request) {
        System.out.println("🔄 발송 취소 요청 받음 - UUID: " + uuid);
        System.out.println("📝 요청 본문: " + request);
        
        String cancelReason = request.get("cancelReason");
        System.out.println("📝 취소 사유: " + cancelReason);
        
        // 이메일 DTO 조회
        EmailDto emailDto = emailRedisTemplate.opsForValue().get("email:draft:" + uuid);
        if (emailDto == null) {
            System.out.println("❌ 초안 없음 (uuid: " + uuid + ")");
            return ResponseEntity.badRequest().body("❌ 초안 없음 (uuid: " + uuid + ")");
        }
        
        System.out.println("✅ 이메일 조회 성공");
        
        // Agent에게 재작성 요청 (기존 UUID의 내용을 업데이트)
        try {
            System.out.println("🤖 Agent 재작성 요청 시작");
            emailService.requestEmailRewriteAndUpdate(emailDto, cancelReason, uuid);
            System.out.println("✅ Agent 재작성 요청 완료");
            return ResponseEntity.ok("✅ 재작성 요청 전송 완료 (uuid: " + uuid + ")");
        } catch (Exception e) {
            System.err.println("❌ Agent 재작성 요청 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.ok("✅ 재작성 요청 실패: " + e.getMessage());
        }
    }
    
    //Agent에서 재작성된 이메일을 세션에 저장
    @PostMapping("/save-to-session")
    public ResponseEntity<String> saveEmailToSession(@RequestBody EmailDto emailDto) {
        System.out.println("🔄 Agent에서 재작성된 이메일 세션 저장 요청: " + emailDto);
        
        try {
            // UUID 생성
            String uuid = java.util.UUID.randomUUID().toString();
            
            // 이메일을 Redis에 저장
            emailRedisTemplate.opsForValue().set("email:draft:" + uuid, emailDto);
            
            // 세션 ID 찾기 (기존 세션이 있는지 확인)
            String sessionId = emailDraftRedisService.findSessionIdByLeadId(emailDto.getLeadId());
            
            if (sessionId == null) {
                // 새 세션 생성
                sessionId = java.util.UUID.randomUUID().toString();
                System.out.println("🆕 새 세션 생성: " + sessionId);
            } else {
                System.out.println("📝 기존 세션 사용: " + sessionId);
            }
            
            // 세션에 UUID 추가
            stringRedisTemplate.opsForList().rightPush("email:draft:session:" + sessionId, uuid);
            
            System.out.println("✅ 재작성된 이메일 세션 저장 완료 - UUID: " + uuid + ", Session: " + sessionId);
            return ResponseEntity.ok("재작성된 이메일이 세션에 저장되었습니다. (UUID: " + uuid + ")");
            
        } catch (Exception e) {
            System.err.println("❌ 재작성된 이메일 세션 저장 실패: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("재작성된 이메일 저장 실패: " + e.getMessage());
        }
    }
    
    
    
    //일괄 발송
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
    
    @GetMapping("/receive")
    public ResponseEntity<List<Map<String, String>>> getUnreadEmails() {
        List<Map<String, String>> unreadEmails = emailService.receiveEmails();
        return ResponseEntity.ok(unreadEmails);
    }
    
    
    //세션의 취소된 이메일들 삭제
    @DeleteMapping("/cleanup-cancelled/{sessionId}")
    public ResponseEntity<String> cleanupCancelledEmails(@PathVariable("sessionId") String sessionId) {
        System.out.println("🧹 세션 " + sessionId + "의 취소된 이메일 정리 시작");
        
        try {
            // 세션의 모든 UUID 조회
            List<String> uuids = stringRedisTemplate.opsForList().range("email:draft:session:" + sessionId, 0, -1);
            if (uuids == null || uuids.isEmpty()) {
                return ResponseEntity.ok("세션에 이메일이 없습니다.");
            }
            
            int deletedCount = 0;
            for (String uuid : uuids) {
                // cancelled 키가 있는지 확인하고 삭제
                if (emailRedisTemplate.hasKey("email:cancelled:" + uuid)) {
                    emailRedisTemplate.delete("email:cancelled:" + uuid);
                    deletedCount++;
                    System.out.println("🗑️ 취소된 이메일 삭제: " + uuid);
                }
            }
            
            System.out.println("✅ 총 " + deletedCount + "개의 취소된 이메일 삭제 완료");
            return ResponseEntity.ok("총 " + deletedCount + "개의 취소된 이메일을 삭제했습니다.");
            
        } catch (Exception e) {
            System.err.println("❌ 취소된 이메일 정리 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body("취소된 이메일 정리 실패: " + e.getMessage());
        }
    }
    
    
    @GetMapping("/draft/{uuid}")
    public ResponseEntity<Map<String, Object>> getDraft(@PathVariable("uuid") String uuid) {
        EmailDto dto = emailRedisTemplate.opsForValue().get("email:draft:" + uuid);
        if (dto == null) return ResponseEntity.notFound().build();

        // 취소 상태를 별도 키로 관리한다면 여기서 읽어서 내려줘도 됨
        boolean isCancelled = false; // 필요 시 실제 값으로 대체

        return ResponseEntity.ok(Map.of(
            "uuid", uuid,
            "subject", dto.getSubject(),
            "body", dto.getBody(),
            "contactEmail", dto.getContactEmail(),
            "isCancelled", isCancelled,
            "lastUpdated", System.currentTimeMillis()
        ));
    }
    
    //Follow-up Email 생성
    @PostMapping("/followup")
    public ResponseEntity<String> generateFollowupEmail(@RequestBody Map<String, Object> request) {
        try {
            String sessionId = emailService.generateFollowupEmail(request);
            return ResponseEntity.ok("후속 이메일 생성 완료 - Session ID: " + sessionId);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("후속 이메일 생성 실패: " + e.getMessage());
        }
    }

	// 수동 이메일 수신 테스트 엔드포인트 추가
	@PostMapping("/receive-test")
	public ResponseEntity<String> testReceiveEmails() {
		try {
			List<Map<String, String>> results = emailService.receiveEmails();
			return ResponseEntity.ok("이메일 수신 테스트 완료. 처리된 메일 수: " + results.size());
		} catch (Exception e) {
			return ResponseEntity.status(500).body("이메일 수신 테스트 실패: " + e.getMessage());
		}
	}

	// 특정 발신자 이메일만 처리하는 테스트 엔드포인트
	@PostMapping("/receive-test-specific")
	public ResponseEntity<String> testReceiveSpecificEmails() {
		try {
			List<Map<String, String>> results = emailService.receiveSpecificEmails("telnosgia@gmail.com");
			return ResponseEntity.ok("특정 발신자 이메일 수신 테스트 완료. 처리된 메일 수: " + results.size());
		} catch (Exception e) {
			return ResponseEntity.status(500).body("특정 발신자 이메일 수신 테스트 실패: " + e.getMessage());
		}
	}
}