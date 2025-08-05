package com.AutoSales_Agent.Email;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
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
	@Value("${spring.mail.username}")	
	private String mailUsername;
	@Value("${spring.mail.password}")
	private String mailPassword;

	
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
	
	
}
