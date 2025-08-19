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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailDraftRedisService {
	
	private final RedisTemplate<String, EmailDto> emailRedisTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;
    
    private static final Duration DRAFT_TTL = Duration.ofHours(6);
    
	// 초안 저장 + 세션 키 생성
    public List<EmailDraftWithUuid> storeDraftsForSession(String sessionId, List<EmailDto> emails) {
        if (emails == null || emails.isEmpty()) return List.of();

        List<EmailDraftWithUuid> out = new java.util.ArrayList<>();
        for (EmailDto email : emails) {
            String uuid = java.util.UUID.randomUUID().toString();
            String draftKey = "email:draft:" + uuid;
            String sessionKey = "email:draft:session:" + sessionId;

            // 값은 타입고정 템플릿으로 저장
            emailRedisTemplate.opsForValue().set(draftKey, email, DRAFT_TTL);

            // 세션 리스트에 uuid 추가
            stringRedisTemplate.opsForList().rightPush(sessionKey, uuid);
            stringRedisTemplate.expire(sessionKey, DRAFT_TTL);

            // 역인덱스: uuid -> sessionId
            //stringRedisTemplate.opsForValue()
                //.set("email:draft:sessionByUuid:" + uuid, sessionId, DRAFT_TTL);

            out.add(new EmailDraftWithUuid(uuid, email));
        }
        return out;
    }
    public String storeDrafts(List<EmailDto> emails) {
        String sessionId = UUID.randomUUID().toString();

        for (EmailDto email : emails) {
            String uuid = UUID.randomUUID().toString();
            emailRedisTemplate.opsForValue().set("email:draft:" + uuid, email, DRAFT_TTL);
            stringRedisTemplate.opsForList().rightPush("email:draft:session:" + sessionId, uuid);
            //stringRedisTemplate.opsForValue().set("email:draft:sessionByUuid:" + uuid, sessionId, DRAFT_TTL);
        }
        stringRedisTemplate.expire("email:draft:session:" + sessionId, DRAFT_TTL);
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
        stringRedisTemplate.delete("email:draft:sessionByUuid:" + uuid); 	
    }

    // ✅ 세션 단위로 전체 초안 삭제
    public void deleteDraftsBySession(String sessionId) {
    	String sessionKey = "email:draft:session:" + sessionId;
        List<String> draftIds = stringRedisTemplate.opsForList().range(sessionKey, 0, -1);

        if (draftIds != null) {
            for (String id : draftIds) {
                emailRedisTemplate.delete("email:draft:" + id);
                stringRedisTemplate.delete("email:draft:sessionByUuid:" + id);
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
    
    // ✅ 세션 리스트에서 uuid 제거(LREM)
    public long removeFromSession(String sessionId, String uuid) {
        String sessionKey = "email:draft:session:" + sessionId;
        return stringRedisTemplate.opsForList().remove(sessionKey, 1, uuid);
    }

    // ✅ 세션 TTL 갱신(선택)
    public void touchSessionTtl(String sessionId) {
        stringRedisTemplate.expire("email:draft:session:" + sessionId, DRAFT_TTL);
    }

    public int countDrafts(String sessionId) {
        Long size = stringRedisTemplate.opsForList().size("email:draft:session:" + sessionId);
        return size == null ? 0 : size.intValue();
    }
    
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
