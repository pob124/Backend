package com.AutoSales_Agent;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Repository
@RequiredArgsConstructor
public class ChatStore {

    @Qualifier("customStringRedisTemplate")
    private final RedisTemplate<String, String> redis;
    private final ObjectMapper om = new ObjectMapper();

    private static String key(String sid){ return "chat:log:" + sid; }

    @SneakyThrows
    public void append(String sid, ChatPair pair){
        String json = om.writeValueAsString(pair);
        redis.opsForList().leftPush(key(sid), json);
        // 필요하면 길이 제한: trim(sid, 500);
    }

    public void trim(String sid, long max){
        Long size = redis.opsForList().size(key(sid));
        if (size != null && size > max){
            // 최신 max개만 보존
            redis.opsForList().trim(key(sid), 0, max - 1);
        }
    }

    public List<ChatPair> latest(String sid, long limit){
        List<String> raw = redis.opsForList().range(key(sid), 0, limit - 1);
        if (raw == null) return List.of();
        // LPUSH로 넣었으니 0.. 는 최신→과거. 화면은 과거→최신이 보기 좋아 역순
        List<ChatPair> list = raw.stream().map(js -> {
            try { return om.readValue(js, ChatPair.class); }
            catch(Exception e){ return null; }
        }).filter(Objects::nonNull).collect(Collectors.toList());
        Collections.reverse(list);
        return list;
    }
}