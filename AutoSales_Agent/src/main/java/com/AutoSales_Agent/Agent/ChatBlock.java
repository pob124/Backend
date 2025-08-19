package com.AutoSales_Agent.Agent;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatBlock {
    private String type;    // draft 등
    private String uuid;
    private String to;
    private String subject;
    private String snippet;
}
