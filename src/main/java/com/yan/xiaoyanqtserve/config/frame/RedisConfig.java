package com.yan.xiaoyanqtserve.config.frame;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.validation.constraints.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


/**
 * Redis 配置类
 */
@Configuration
public class RedisConfig {

    private final ObjectMapper objectMapper;

    // 构造器注入
    public RedisConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        // 复制一个 ObjectMapper，避免改到全局配置
        ObjectMapper om = objectMapper.copy();
        // 注册 Java8 时间模块
        om.registerModule(new JavaTimeModule());
        om.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 打开类型信息
        om.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
        );

        RedisTemplate<String, Object> template = getRedisTemplate(connectionFactory, om);

        template.afterPropertiesSet();
        return template;
    }

    @NotNull
    private static RedisTemplate<String, Object> getRedisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper om) {
        // 使用 JSON 序列化对象
        GenericJackson2JsonRedisSerializer jsonSerializer =
                new GenericJackson2JsonRedisSerializer(om);

        // 准备 String 序列化器
        StringRedisSerializer stringSerializer = new StringRedisSerializer();


        // 创建并配置RedisTemplate对象
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setDefaultSerializer(jsonSerializer);
        // key → String
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        // value → JSON (支持 LocalDateTime)
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        return template;
    }
}
