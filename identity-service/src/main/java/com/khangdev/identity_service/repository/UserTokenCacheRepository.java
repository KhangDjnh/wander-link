package com.khangdev.identity_service.repository;

import com.khangdev.identity_service.dto.CachedUserToken;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class UserTokenCacheRepository {

    private final RedisTemplate<String, CachedUserToken> cachedUserTokenRedisTemplate;

    private String key(String email) {
        return "user-token:" + email;
    }

    public CachedUserToken getToken(String email) {
        return cachedUserTokenRedisTemplate.opsForValue().get(key(email));
    }

    public void saveToken(String email, CachedUserToken token, Duration ttl) {
        cachedUserTokenRedisTemplate.opsForValue().set(key(email), token, ttl);
    }

    public void deleteToken(String email) {
        cachedUserTokenRedisTemplate.delete(key(email));
    }
}

