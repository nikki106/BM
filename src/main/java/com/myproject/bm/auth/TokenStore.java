package com.myproject.bm.auth;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存 token 存储, 服务重启后失效, 有效期 24 小时
 */
@Component
public class TokenStore {

    private static final long EXPIRE_MILLIS = 24 * 60 * 60 * 1000L;

    private final Map<String, Entry> tokens = new ConcurrentHashMap<>();

    public String issue(LoginUser user) {
        String token = UUID.randomUUID().toString().replace("-", "");
        tokens.put(token, new Entry(user, System.currentTimeMillis()));
        return token;
    }

    public LoginUser get(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        Entry entry = tokens.get(token);
        if (entry == null) {
            return null;
        }
        if (System.currentTimeMillis() - entry.createdAt > EXPIRE_MILLIS) {
            tokens.remove(token);
            return null;
        }
        return entry.user;
    }

    private record Entry(LoginUser user, long createdAt) {
    }
}
