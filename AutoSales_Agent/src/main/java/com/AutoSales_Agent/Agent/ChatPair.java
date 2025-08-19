package com.AutoSales_Agent.Agent;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatPair {
    private String user;             // 사용자 입력
    private ChatAssistant assistant; // 에이전트 응답
    private long ts;                 // epoch millis
}
