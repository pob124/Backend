package com.AutoSales_Agent.Agent;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.AutoSales_Agent.Agent.AgentService.Result;
import com.AutoSales_Agent.Email.EmailDraftRedisService;
import com.AutoSales_Agent.Email.EmailDraftWithUuid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class AgentController {
    private final AgentService agentService;
    private final ChatStore chatStore;
    private final EmailDraftRedisService draftService; 

    @PostMapping("")
    public ResponseEntity<?> chat(@RequestBody ChatReq req) {
        if (req == null || req.getSid()==null || req.getSid().isBlank()
            || req.getQ()==null || req.getQ().isBlank()) {
            return ResponseEntity.badRequest()
                .body(Map.of("type","ERROR","message","sid/q required"));
        }

        var result = agentService.call(req.getSid(), req.getQ()); 

        List<EmailDraftWithUuid> saved = draftService.storeDraftsForSession(
            req.getSid(), Optional.ofNullable(result.getDrafts()).orElseGet(List::of)
        );

        // 프론트 표시용 block 빌드
        List<Map<String, String>> blocks = saved.stream().map(e -> Map.of(
            "type", "draft",
            "uuid", e.getUuid(),
            "to", nvl(e.getEmail().getContactEmail()),
            "subject", nvl(e.getEmail().getSubject()),
            "snippet", snippet(e.getEmail().getBody())
        )).toList();

        // 대화 저장
        chatStore.append(req.getSid(), ChatPair.builder()
            .user(req.getQ())
            .assistant(ChatAssistant.builder()
                .text(nvl(result.getText()))
                .blocks(saved.stream().map(e ->
                    ChatBlock.builder()
                        .type("draft")
                        .uuid(e.getUuid())
                        .to(nvl(e.getEmail().getContactEmail()))
                        .subject(nvl(e.getEmail().getSubject()))
                        .snippet(snippet(e.getEmail().getBody()))
                        .build()
                ).toList()).build())
            .ts(System.currentTimeMillis())
            .build());
        chatStore.trim(req.getSid(), 500);

        return ResponseEntity.ok(Map.of(
            "type","ASSISTANT_ONLY",
            "assistant", Map.of("text", nvl(result.getText()), "blocks", blocks)
        ));
    }

    @GetMapping("/history")
    public ResponseEntity<?> history(
        @RequestParam(name="sid") String sid,
        @RequestParam(name="limit", defaultValue="30") long limit
    ){
        return ResponseEntity.ok(chatStore.latest(sid, limit));
    }

    private static String nvl(String s){ return s==null? "": s; }
    private static String snippet(String s){ if(s==null) return ""; s=s.strip(); return s.length()>500?s.substring(0,500)+" ...":s; }
}		
