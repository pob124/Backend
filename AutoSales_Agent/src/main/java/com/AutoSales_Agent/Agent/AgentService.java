package com.AutoSales_Agent.Agent;

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

    public Result call(String sid, String prompt) {
        System.out.println("🔍 AgentService.call 호출됨 - sid: " + sid + ", prompt: " + prompt);
        Result result = http.post()
                .uri("/agent/handle")
                .body(new Req(sid, prompt))
                .retrieve()
                .body(Result.class);
        System.out.println("🔍 AgentService.call 결과: " + result);
        return result;
    }

    @Data static class Req { 
    	final String sid; final String prompt; 
    }

    @Data 
    public static class Result {
        private String intent;         
        private String text;           
        private List<EmailDto> drafts; // 초안들(없을 수 있음) — EmailDto 그대로 받음
    }
}
