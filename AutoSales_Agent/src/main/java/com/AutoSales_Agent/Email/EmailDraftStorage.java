package com.AutoSales_Agent.Email;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Component;

@Component
public class EmailDraftStorage {
	// 메모리 기반 임시 저장소 (실제 운영에서는 Redis나 DB 사용 권장)
    private final ConcurrentMap<String, List<EmailDto>> draftStore = new ConcurrentHashMap<>();
    private static final String DEFAULT_KEY = "temp_drafts";
    
    public void storeEmails(List<EmailDto> emails) {
        draftStore.put(DEFAULT_KEY, emails);
        System.out.println("💾 DraftStorage에 " + emails.size() + "개 이메일 저장됨");
    }
    
    public List<EmailDto> getStoredEmails() {
        List<EmailDto> emails = draftStore.get(DEFAULT_KEY);
        System.out.println("📤 DraftStorage에서 " + (emails != null ? emails.size() : 0) + "개 이메일 조회됨");
        return emails;
    }
    
    public void clearStoredEmails() {
        draftStore.remove(DEFAULT_KEY);
		System.out.println("🗑️ DraftStorage 초기화됨");
    }
    
    public boolean hasStoredEmails() {
        List<EmailDto> emails = draftStore.get(DEFAULT_KEY);
        return emails != null && !emails.isEmpty();
    }
}
