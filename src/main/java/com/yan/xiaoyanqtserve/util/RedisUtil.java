package com.yan.xiaoyanqtserve.util;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类，封装常用操作
 * 支持 String、Hash、Set、List、ZSet 类型操作
 */
@Slf4j
@Component
public class RedisUtil {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    // ============================= Key =============================

    /**
     * 判断 key 是否存在
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 删除指定 key
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 批量删除多个 key
     */
    public Long delete(Collection<String> keys) {
        return redisTemplate.delete(keys);
    }

    /**
     * 设置 key 的过期时间
     */
    public void expire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 获取 key 的剩余过期时间（单位：秒）
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }


    // ============================= String =============================

    /**
     * 设置字符串键值
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置字符串键值，并指定过期时间
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 获取字符串值
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 递增数值（用于计数器等）
     */
    public Long incr(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 递减数值
     */
    public Long decr(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, -delta);
    }

    // ============================= Hash =============================

    /**
     * 设置 Hash 中的一个字段值
     */
    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * 获取 Hash 中指定字段的值
     */
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 获取 Hash 中所有字段和值
     */
    public Map<Object, Object> hGetAll(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 获取 Hash 中所有字段名
     */
    public Set<Object> hGetAllFields(String key) {
        return redisTemplate.opsForHash().keys(key);
    }

    /**
     * 获取 Hash 中所有字段的值
     */
    public List<Object> hGetAllValues(String key) {
        return redisTemplate.opsForHash().values(key);
    }
    /**
     * 删除 Hash 中的字段
     */
    public void hDel(String key, Object... fields) {
        redisTemplate.opsForHash().delete(key, fields);
    }

    /**
     * 判断 Hash 中是否存在某个字段
     */
    public Boolean hHasKey(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    // ============================= Set =============================

    /**
     * 向 Set 添加元素
     */
    public Long sAdd(String key, Object... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * 判断值是否是 Set 成员
     */
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 获取 Set 所有成员
     */
    public Set<Object> sMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 从 Set 中移除元素
     */
    public void sRemove(String key, Object... values) {
        redisTemplate.opsForSet().remove(key, values);
    }

    // ============================= List =============================

    /**
     * 将元素插入 List 的左边（头部）
     */
    public void lPush(String key, Object value) {
        redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 弹出 List 左边（头部）元素
     */
    public Object lPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    /**
     * 弹出 List 右边（尾部）元素
     */
    public Object rPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    /**
     * 获取 List 的长度
     */
    public Long lSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    // ============================= ZSet =============================

    /**
     * 向 ZSet 添加元素及分数（如排行榜）
     */
    public Boolean zAdd(String key, Object value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 删除 ZSet 中的元素
     */
    public Long zRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    /**
     * 获取 ZSet 中指定区间的元素（按分数升序）
     */
    public Set<Object> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * 查询 ZSet 中 score 在 [min, max] 范围内的所有 member
     */
    public Set<Object> zRangeByScore(String key, long min, long max) {
        return redisTemplate.opsForZSet().rangeByScore(key, (double) min, (double) max);
    }
    /**
     * 获取 ZSet 中指定区间的元素（按分数降序）
     */
    public Set<Object> zRevRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * 获取 ZSet 中指定成员的分数
     */
    public Double zScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * 获取 ZSet 中成员的排名（从 0 开始，升序）
     */
    public Long zRank(String key, Object value) {
        return redisTemplate.opsForZSet().rank(key, value);
    }

    /**
     * 获取 ZSet 中成员的排名（从 0 开始，降序）
     */
    public Long zRevRank(String key, Object value) {
        return redisTemplate.opsForZSet().reverseRank(key, value);
    }

    /**
     * 获取 ZSet 中分数在指定区间的成员数量
     */
    public Long zCount(String key, double min, double max) {
        return redisTemplate.opsForZSet().count(key, min, max);
    }

}
