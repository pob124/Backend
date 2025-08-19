package com.AutoSales_Agent.Agent;

import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatAssistant {
    private String text;
    private List<ChatBlock> blocks;
}