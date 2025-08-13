package com.AutoSales_Agent.Email;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class EmailDraftRedisService {
	
	private final RedisTemplate<String, EmailDto> emailRedisTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;
    
    // 생성자에서 @Qualifier 사용
    public EmailDraftRedisService(RedisTemplate<String, EmailDto> emailRedisTemplate,
                                 @Qualifier("customStringRedisTemplate") RedisTemplate<String, String> stringRedisTemplate) {
        this.emailRedisTemplate = emailRedisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }
	
	// ✅ 초안 저장 + 세션 키 생성
    public String storeDrafts(List<EmailDto> emails) {
        String sessionId = UUID.randomUUID().toString();

        for (EmailDto email : emails) {
            String uuid = UUID.randomUUID().toString();
            emailRedisTemplate.opsForValue().set("email:draft:" + uuid, email, Duration.ofHours(6));
            stringRedisTemplate.opsForList().rightPush("email:draft:session:" + sessionId, uuid);
        }
        stringRedisTemplate.expire("email:draft:session:" + sessionId, Duration.ofHours(6));
        return sessionId;
    }
	 
    // ✅ 세션 ID로 전체 초안 가져오기
    public List<EmailDraftWithUuid> getDrafts(String sessionId) {
    	 List<String> draftIds = stringRedisTemplate.opsForList()
    		        .range("email:draft:session:" + sessionId, 0, -1);

    	 if (draftIds == null) 
    		 return Collections.emptyList();

    	 return draftIds.stream()
    			 .map(id -> {
    				 EmailDto dto = emailRedisTemplate.opsForValue().get("email:draft:" + id);
    				 return dto != null ? new EmailDraftWithUuid(id, dto) : null;
    			 })
    			 .filter(Objects::nonNull)
    			 .collect(Collectors.toList());
    }
	 
	    
    // ✅ 개별 초안 삭제
    public void deleteSingleDraft(String uuid) {
        emailRedisTemplate.delete("email:draft:" + uuid);
    }

    // ✅ 세션 단위로 전체 초안 삭제
    public void deleteDraftsBySession(String sessionId) {
        List<String> draftIds = stringRedisTemplate.opsForList().range("email:draft:session:" + sessionId, 0, -1);

        if (draftIds != null) {
            for (String id : draftIds) {
                emailRedisTemplate.delete("email:draft:" + id);
            }
        }

        stringRedisTemplate.delete("email:draft:session:" + sessionId);
    }
    
    public String findSessionIdByUuid(String uuid) {
        Set<String> keys = stringRedisTemplate.keys("email:draft:session:*");
        for (String key : keys) {
            List<String> uuids = stringRedisTemplate.opsForList().range(key, 0, -1);
            if (uuids != null && uuids.contains(uuid)) {
                return key.replace("email:draft:session:", "");
            }
        }
        return null;
    }
    
    // ✅ Lead ID로 세션 ID 찾기
    public String findSessionIdByLeadId(Integer leadId) {
        Set<String> keys = stringRedisTemplate.keys("email:draft:session:*");
        for (String key : keys) {
            List<String> uuids = stringRedisTemplate.opsForList().range(key, 0, -1);
            if (uuids != null) {
                for (String uuid : uuids) {
                    EmailDto emailDto = emailRedisTemplate.opsForValue().get("email:draft:" + uuid);
                    if (emailDto != null && emailDto.getLeadId().equals(leadId)) {
                        return key.replace("email:draft:session:", "");
                    }
                }
            }
        }
        return null;
    }

    public int countDrafts(String sessionId) {
        Long size = stringRedisTemplate.opsForList().size("email:draft:session:" + sessionId);
        return size == null ? 0 : size.intValue();
    }
    
    // ✅ 취소된 이메일 조회
    public List<EmailDraftWithUuid> getCancelledEmails(String sessionId) {
        List<String> draftIds = stringRedisTemplate.opsForList()
                .range("email:draft:session:" + sessionId, 0, -1);

        if (draftIds == null) 
            return Collections.emptyList();

        return draftIds.stream()
                .map(id -> {
                    // 먼저 draft에서 찾고, 없으면 cancelled에서 찾기
                    EmailDto dto = emailRedisTemplate.opsForValue().get("email:draft:" + id);
                    boolean isCancelled = false;
                    
                    if (dto == null) {
                        dto = emailRedisTemplate.opsForValue().get("email:cancelled:" + id);
                        isCancelled = true;
                    }
                    
                    return dto != null ? new EmailDraftWithUuid(id, dto, isCancelled) : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
