package com.swigg.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public <T> T getOrElse(String key, Class<T> clazz, Supplier<T> supplier, long ttlSeconds) {
        try {
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return clazz.cast(cached);
            }
        } catch (Exception e) {
            // Log warning or error but fallback to supplier
        }

        T result = supplier.get();
        if (result != null) {
            try {
                redisTemplate.opsForValue().set(key, result, ttlSeconds, TimeUnit.SECONDS);
            } catch (Exception e) {
                // Log warning or error
            }
        }
        return result;
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            // Log warning or error
        }
    }
}
