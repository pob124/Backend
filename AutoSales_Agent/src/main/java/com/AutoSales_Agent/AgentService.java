package com.AutoSales_Agent;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.AutoSales_Agent.Email.EmailDto;

import lombok.Data;


@Service 
public class AgentService{
	private final RestClient http;

    public AgentService(@Value("${agent.base-url}") String baseUrl) {
        this.http = RestClient.builder().baseUrl(baseUrl).build();
    }

    /** TS 에이전트의 /agent/handle 호출 (동기) */
    public Result call(String sid, String prompt) {
        return http.post()
                .uri("/agent/handle")
                .body(new Req(sid, prompt))
                .retrieve()
                .body(Result.class);
    }

    @Data static class Req { 
    	final String sid; final String prompt; 
    }

    @Data 
    public static class Result {
        private String intent;         // 선택
        private String text;           // 어시스턴트 텍스트
        private List<EmailDto> drafts; // 초안들(없을 수 있음) — EmailDto 그대로 받음
    }
}
