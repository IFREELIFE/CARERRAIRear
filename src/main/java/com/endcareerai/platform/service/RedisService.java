package com.endcareerai.platform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存服务
 * 封装 Redis 常用操作，提供键值存取、删除、判断和自增功能，
 * 用于用户信息缓存、岗位缓存、教师时间段缓存等场景
 */
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 存入缓存（指定过期时间和时间单位）
     *
     * @param key     缓存键
     * @param value   缓存值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 存入缓存（默认30分钟过期）
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value, 30, TimeUnit.MINUTES);
    }

    /**
     * 根据键获取缓存值
     *
     * @param key 缓存键
     * @return 缓存值，不存在时返回 null
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 删除指定键的缓存
     *
     * @param key 缓存键
     * @return 是否删除成功
     */
    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    /**
     * 判断指定键是否存在
     *
     * @param key 缓存键
     * @return 是否存在
     */
    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 对指定键的值执行自增操作（+1）
     *
     * @param key 缓存键
     */
    public void increment(String key) {
        redisTemplate.opsForValue().increment(key);
    }
}
