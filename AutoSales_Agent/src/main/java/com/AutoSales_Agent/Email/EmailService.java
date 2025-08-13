package com.AutoSales_Agent.Email;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.AutoSales_Agent.Feedback.FeedbackDto;
import com.AutoSales_Agent.Feedback.FeedbackService;
import com.AutoSales_Agent.Lead.Lead;
import com.AutoSales_Agent.Lead.LeadRepository;
import com.AutoSales_Agent.Lead.LeadService;
import com.AutoSales_Agent.Project.Project;
import com.AutoSales_Agent.Project.ProjectRepository;
import com.AutoSales_Agent.Project.ProjectService;

import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.FlagTerm;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

	private final EmailRepository emailRepository;
	private final ProjectService projectService;
	private final LeadService leadService;
	private final LeadRepository leadRepository;
	private final JavaMailSender mailSender;
	private final ProjectRepository projectRepository;
	private final FeedbackService feedbackService;
	private final RestTemplate restTemplate;
	private final RedisTemplate<String, EmailDto> emailRedisTemplate;
	private final RedisTemplate<String, String> stringRedisTemplate;
	private final EmailDraftRedisService emailDraftRedisService;
	@Value("${spring.mail.username}")	
	private String mailUsername;
	@Value("${spring.mail.password}")
	private String mailPassword;
	
	// 재작성 실패 카운터 (UUID별로 관리)
	private final Map<String, Integer> rewriteFailureCount = new HashMap<>();
	private static final int MAX_REWRITE_FAILURES = 4;

	
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
		email.setSent(false);
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
			Email savedEmail = save(dto);
			Integer emailId = savedEmail.getId();
			
			String decoratedBody = "<html><body>" + dto.getBody() + "<!-- emailId:" + emailId + " --></body></html>";
			System.out.println("보낸 메일: " + decoratedBody);
			MimeMessage message = mailSender.createMimeMessage();
	        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

	        helper.setTo(to);
	        helper.setSubject("[emailId:" + emailId + "] " + dto.getSubject());
	        helper.setText(decoratedBody, true);
	        helper.setFrom("sks02040204@gmail.com");
	        
	        mailSender.send(message);
	        System.out.println("✅ 메일 전송 성공: " + to);
	        
	        savedEmail.setSent(true);
	        emailRepository.save(savedEmail);
		}catch(Exception e){
			 System.err.println("❌ 메일 전송 실패: " + e.getMessage());
	         throw new RuntimeException("메일 전송 실패");
		}
	}
	
	//3시간마다 메일 자동으로 읽어옴.
	//@Scheduled(cron = "0 0 7,10,13,15,17,18 * * *")
	@Scheduled(fixedRate =1 * 60 * 1000)
	public void scheduleReceiveEmails() {
	    System.out.println("[메일 수신]");
	    receiveEmails();
	}
	
	public List<Map<String, String>> receiveEmails(){
		List<Map<String, String>> result = new ArrayList<>();
		String host = "imap.gmail.com";
		
		Properties props = new Properties();
        props.put("mail.store.protocol", "imaps");
        
        try {
        	Session session = Session.getInstance(props);
        	Store store = session.getStore();
        	store.connect(host, mailUsername, mailPassword);
        	
        	Folder inbox = store.getFolder("INBOX");
        	inbox.open(Folder.READ_WRITE);
        	//inbox.open(Folder.READ_ONLY);
            Message[] messages = inbox.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false));
            System.out.println("읽지 않은 메일 수: " + messages.length);
            
            for (Message message : messages) {
                Address[] froms = message.getFrom();
                
                if (froms == null || froms.length == 0) 
                	continue;

                String senderEmail = ((InternetAddress) froms[0]).getAddress();
                // 💡 이메일로 리드 조회
                Optional<Lead> optionalLead = leadRepository.findByContactEmail(senderEmail);
                if (optionalLead.isEmpty()) 
                	continue;
                
                Lead lead = optionalLead.get();
                Integer leadId = lead.getId();
                String leadName = lead.getName();

                Integer projectId = this.projectService.findProjectForFeedback(leadId);
                Project project = projectRepository.findById(projectId).orElse(null);
                String projectName = projectRepository.findById(projectId)
                                    .map(Project::getName)
                                    .orElse("Unknown");
                
                String subject = message.getSubject();
                String body = "";

                Object content = message.getContent();
                if (content instanceof String str) {
                    body = str;
                } 
                else if (content instanceof Multipart multipart) {
                    for (int i = 0; i < multipart.getCount(); i++) {
                        BodyPart part = multipart.getBodyPart(i);
                        if (part.isMimeType("text/plain")) {
                            body = part.getContent().toString();
                            break;
                        }
                    }
                }
                
                Integer emailId = null;
                Matcher bodyMatcher = Pattern.compile("<!--\\s*emailId\\s*:\\s*(\\d+)\\s*-->").matcher(body);
                if (bodyMatcher.find()) {
                    emailId = Integer.parseInt(bodyMatcher.group(1));
                }
                
                if (emailId == null) {
                    Matcher subjectMatcher = Pattern.compile("\\[emailId:(\\d+)]").matcher(subject);
                    if (subjectMatcher.find()) {
                        emailId = Integer.parseInt(subjectMatcher.group(1));
                    }
                }
                
                Email email = null;
                if (emailId != null) {
                    email = emailRepository.findById(emailId).orElse(null);
                }
                
                Map<String, String> agentResult = callAgentForFeedbackSummary(
                        leadName, projectName, subject, body
                    );
                
                if (agentResult != null && project != null) {
                	System.out.println("🧠 Agent 응답: " + agentResult);
                	
                    String summary = agentResult.get("summary");
                    String responseType = agentResult.get("responseType");

                    FeedbackDto dto = new FeedbackDto();
                    dto.setLeadId(leadId);
                    dto.setProjectId(projectId);
                    dto.setEmailId(emailId); // 아직 메일 연동 안 됐으므로 null
                    dto.setOriginalText(body);
                    dto.setResponseSummary(summary);
                    dto.setResponseType(responseType);

                    feedbackService.saveFeedback(dto);
                    System.out.println("✅ 분석 결과 저장 완료: " + summary + " (" + responseType + ")");
                }
                
                message.setFlag(Flags.Flag.SEEN, true); // 읽음 처리(지금은 아님 테스트용)

                // 디버깅 출력
                System.out.println("📬 From: " + senderEmail);
                System.out.println("🏢 Lead: " + leadName);
                System.out.println("📁 Project: " + projectName);
                System.out.println("📝 Subject: " + subject);
                System.out.println("📄 Body: " + body);
                System.out.println("------");
            }

            inbox.close(false);
            store.close();

        } catch (Exception e) {
            System.err.println("❌ 메일 수신 중 오류 발생:");
            e.printStackTrace();
        }

        return result;
    }
	
	public Map<String, String> callAgentForFeedbackSummary(String leadName, String projectName, String subject, String body) {
	    try {
	        Map<String, String> request = new HashMap<>();
	        request.put("leadName", leadName);
	        request.put("projectName", projectName);
	        request.put("subject", subject);
	        request.put("body", body);

	        ResponseEntity<Map> response = restTemplate.postForEntity(
	        	"http://localhost:3000/feedback/summarize",
	            request,
	            Map.class
	        );

	        return response.getBody();
	    } catch (Exception e) {
	        System.err.println("❌ Agent 호출 실패: " + e.getMessage());
	        return null;
	    }
	}
	
	// 이메일 재작성 요청을 Agent에게 전송하고 결과를 세션에 저장
	public void requestEmailRewrite(EmailDto emailDto, String cancelReason, String sessionId) {
	    try {
	                String agentMessage = String.format(
            "재작성요청 projectId=%d leadId=%d originalEmail={\"subject\":\"%s\",\"body\":\"%s\"} userFeedback=\"발송 취소 사유: %s\"",
            emailDto.getProjectId(),
            emailDto.getLeadId(),
            emailDto.getSubject().replace("\"", "\\\""),
            emailDto.getBody().replace("\"", "\\\""),
            cancelReason != null ? cancelReason.replace("\"", "\\\"") : "사용자가 발송을 취소함"
        );
	        
	        ResponseEntity<Map> response = restTemplate.postForEntity(
	            "http://localhost:3000/chatbot",
	            Map.of("message", agentMessage),
	            Map.class
	        );
	        
	        System.out.println("✅ Agent 재작성 요청 전송 완료: " + agentMessage);
	        
	        // Agent 응답에서 재작성된 이메일 정보 추출 및 세션에 저장
	        if (response.getBody() != null) {
	            Map<String, Object> agentResponse = response.getBody();
	            if (agentResponse.containsKey("subject") && agentResponse.containsKey("body")) {
	                // 재작성된 이메일을 새로운 UUID로 세션에 저장
	                EmailDto rewrittenEmail = new EmailDto();
	                rewrittenEmail.setProjectId(emailDto.getProjectId());
	                rewrittenEmail.setLeadId(emailDto.getLeadId());
	                rewrittenEmail.setSubject((String) agentResponse.get("subject"));
	                rewrittenEmail.setBody((String) agentResponse.get("body"));
	                rewrittenEmail.setContactEmail(emailDto.getContactEmail());
	                
	                // 새로운 UUID 생성하여 세션에 저장
	                String newUuid = java.util.UUID.randomUUID().toString();
	                emailRedisTemplate.opsForValue().set("email:draft:" + newUuid, rewrittenEmail);
	                
	                // 전달받은 세션 ID를 사용하여 같은 세션에 저장
	                if (sessionId != null) {
	                    stringRedisTemplate.opsForList().rightPush("email:draft:session:" + sessionId, newUuid);
	                    System.out.println("✅ 재작성된 이메일을 기존 세션에 저장 완료 (UUID: " + newUuid + ", Session: " + sessionId + ")");
	                } else {
	                    // 세션 ID가 없는 경우 새로운 세션 생성
	                    String newSessionId = java.util.UUID.randomUUID().toString();
	                    stringRedisTemplate.opsForList().rightPush("email:draft:session:" + newSessionId, newUuid);
	                    System.out.println("✅ 재작성된 이메일을 새 세션에 저장 완료 (UUID: " + newUuid + ", New Session: " + newSessionId + ")");
	                }
	            }
	        }
	    } catch (Exception e) {
	        System.err.println("❌ Agent 재작성 요청 실패: " + e.getMessage());
	        throw new RuntimeException("Agent 재작성 요청 실패", e);
	    }
	}
	
	// 이메일 재작성 요청을 Agent에게 전송하고 기존 UUID의 내용을 업데이트
	public void requestEmailRewriteAndUpdate(EmailDto emailDto, String cancelReason, String uuid) {
	    try {
	        // 재작성 실패 횟수 확인
	        int failureCount = rewriteFailureCount.getOrDefault(uuid, 0);
	        if (failureCount >= MAX_REWRITE_FAILURES) {
	            System.out.println("⚠️ 재작성 실패 횟수 초과 (UUID: " + uuid + ", 실패: " + failureCount + "/" + MAX_REWRITE_FAILURES + "회) - 재작성 건너뜀");
	            System.out.println("💡 프론트엔드에서 '선택 재작성' 버튼을 다시 누르면 실패 카운터가 초기화됩니다.");
	            return;
	        }
	        
	        // 이미 재작성 중인지 확인 (무한 루프 방지)
	        if (emailDto.getSubject().contains("[재작성]") || emailDto.getBody().contains("재작성")) {
	            System.out.println("⚠️ 이미 재작성된 이메일입니다. 재작성 건너뜀");
	            return;
	        }
	        
	        String agentMessage = String.format(
	            "재작성요청 projectId=%d leadId=%d originalEmail={\"subject\":\"%s\",\"body\":\"%s\"} userFeedback=\"발송 취소 사유: %s\"",
	            emailDto.getProjectId(),
	            emailDto.getLeadId(),
	            emailDto.getSubject().replace("\"", "\\\""),
	            emailDto.getBody().replace("\"", "\\\""),
	            cancelReason != null ? cancelReason.replace("\"", "\\\"") : "사용자가 발송을 취소함"
	        );
	        
	        ResponseEntity<Map> response = restTemplate.postForEntity(
	            "http://localhost:3000/chatbot",
	            Map.of("message", agentMessage),
	            Map.class
	        );
	        
	        System.out.println("✅ Agent 재작성 요청 전송 완료: " + agentMessage);
	        
	        // Agent 응답에서 재작성된 이메일 정보 추출 및 기존 UUID 업데이트
	        if (response.getBody() != null) {
	            Map<String, Object> agentResponse = response.getBody();
	            if (agentResponse.containsKey("subject") && agentResponse.containsKey("body")) {
	                // 기존 UUID의 이메일 내용을 재작성된 내용으로 업데이트
	                EmailDto rewrittenEmail = new EmailDto();
	                rewrittenEmail.setProjectId(emailDto.getProjectId());
	                rewrittenEmail.setLeadId(emailDto.getLeadId());
	                rewrittenEmail.setSubject("[재작성] " + (String) agentResponse.get("subject"));
	                rewrittenEmail.setBody((String) agentResponse.get("body"));
	                rewrittenEmail.setContactEmail(emailDto.getContactEmail());
	                
	                // 기존 UUID에 재작성된 내용 저장
	                emailRedisTemplate.opsForValue().set("email:draft:" + uuid, rewrittenEmail);
	                
	                System.out.println("✅ 기존 UUID에 재작성된 이메일 내용 업데이트 완료 (UUID: " + uuid + ")");
	                // 성공 시 실패 카운터 초기화
	                rewriteFailureCount.remove(uuid);
	            } else {
	                System.err.println("❌ Agent 응답에 subject 또는 body가 없습니다: " + agentResponse);
	                // 실패 카운터 증가
	                incrementFailureCount(uuid);
	            }
	        } else {
	            System.err.println("❌ Agent 응답이 null입니다");
	            // 실패 카운터 증가
	            incrementFailureCount(uuid);
	        }
	    } catch (Exception e) {
	        System.err.println("❌ Agent 재작성 요청 실패: " + e.getMessage());
	        // 실패 카운터 증가
	        incrementFailureCount(uuid);
	        throw new RuntimeException("Agent 재작성 요청 실패", e);
	    }
	}
	
	// Lead ID로 세션 ID를 찾는 헬퍼 메서드
	private String findSessionIdForLead(Integer leadId) {
	    try {
	        // 모든 세션 키를 조회
	        Set<String> sessionKeys = stringRedisTemplate.keys("email:draft:session:*");
	        if (sessionKeys != null) {
	            for (String sessionKey : sessionKeys) {
	                String sessionId = sessionKey.replace("email:draft:session:", "");
	                List<String> uuids = stringRedisTemplate.opsForList().range(sessionKey, 0, -1);
	                if (uuids != null) {
	                    for (String uuid : uuids) {
	                        EmailDto emailDto = emailRedisTemplate.opsForValue().get("email:draft:" + uuid);
	                        if (emailDto != null && emailDto.getLeadId().equals(leadId)) {
	                            return sessionId;
	                        }
	                    }
	                }
	            }
	        }
	    } catch (Exception e) {
	        System.err.println("❌ 세션 ID 찾기 실패: " + e.getMessage());
	    }
	    return null;
	}
	
	// 재작성 실패 카운터 증가
		private void incrementFailureCount(String uuid) {
		int currentCount = rewriteFailureCount.getOrDefault(uuid, 0);
		rewriteFailureCount.put(uuid, currentCount + 1);
		System.out.println("⚠️ 재작성 실패 카운터 증가 (UUID: " + uuid + ", 실패: " + (currentCount + 1) + "/" + MAX_REWRITE_FAILURES + ")");
	}
	
	// 재작성 실패 카운터 초기화
	public void resetRewriteFailureCount() {
		rewriteFailureCount.clear();
		System.out.println("✅ 재작성 실패 카운터 초기화 완료");
	}
	
	
}
