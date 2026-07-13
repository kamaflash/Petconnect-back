package com.petconnect.shared.infrastructure.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CacheService {

    private static final String TOKEN_BLACKLIST_PREFIX = "blacklist:token:";
    private static final String HOME_CACHE_PREFIX = "cache:home:";
    private static final String SESSION_PREFIX = "session:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;

    public CacheService(
            @Autowired(required = false) RedisTemplate<String, Object> redisTemplate,
            @Autowired(required = false) StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    // ========== TOKEN BLACKLIST (for logout) ==========

    public void blacklistToken(String token, long ttlMillis) {
        if (stringRedisTemplate != null) {
            stringRedisTemplate.opsForValue()
                    .set(TOKEN_BLACKLIST_PREFIX + token, "true", ttlMillis, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isTokenBlacklisted(String token) {
        if (stringRedisTemplate == null) {
            return false;
        }
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token));
    }

    // ========== HOME DATA CACHE ==========

    public void cacheHomeData(String key, Object data, long ttlSeconds) {
        if (redisTemplate != null) {
            redisTemplate.opsForValue()
                    .set(HOME_CACHE_PREFIX + key, data, ttlSeconds, TimeUnit.SECONDS);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getHomeData(String key, Class<T> type) {
        if (redisTemplate == null) {
            return null;
        }
        var data = redisTemplate.opsForValue().get(HOME_CACHE_PREFIX + key);
        if (type.isInstance(data)) {
            return (T) data;
        }
        return null;
    }

    public void evictHomeCache(String key) {
        if (redisTemplate != null) {
            redisTemplate.delete(HOME_CACHE_PREFIX + key);
        }
    }

    public void evictAllHomeCache() {
        if (redisTemplate != null) {
            var keys = redisTemplate.keys(HOME_CACHE_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        }
    }

    // ========== SESSION MANAGEMENT ==========

    public void storeRefreshToken(String userId, String refreshToken, long ttlMillis) {
        if (stringRedisTemplate != null) {
            stringRedisTemplate.opsForValue()
                    .set(SESSION_PREFIX + userId, refreshToken, ttlMillis, TimeUnit.MILLISECONDS);
        }
    }

    public String getRefreshToken(String userId) {
        if (stringRedisTemplate == null) {
            return null;
        }
        return stringRedisTemplate.opsForValue().get(SESSION_PREFIX + userId);
    }

    public void removeSession(String userId) {
        if (stringRedisTemplate != null) {
            stringRedisTemplate.delete(SESSION_PREFIX + userId);
        }
    }
}
