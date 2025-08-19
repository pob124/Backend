package com.AutoSales_Agent.Agent;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatReq {
    private String sid;
    private String q;
}
