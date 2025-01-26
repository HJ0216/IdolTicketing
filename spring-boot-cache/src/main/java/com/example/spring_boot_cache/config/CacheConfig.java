package com.example.spring_boot_cache.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Duration;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class CacheConfig {

  public static final String CACHE1 = "cache1";
  public static final String CACHE2 = "cache2";

  @AllArgsConstructor
  @Getter
  public static class CacheProperty{
    private String name;
    private Integer ttl;
  }

  @Bean
  public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(){
    // RedisCacheManagerBuilderCustomizer: Redis 캐시의 직렬화 방식, 만료 시간, null 값 캐싱 등을 설정
    PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator
        .builder()
        .allowIfSubType(Object.class)
        .build();
    // PolymorphicTypeValidator: allowIfSubType을 통해 변환할 수 있는 데이터 타입을 정의

    ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(new JavaTimeModule())
        .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL)
        .disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
    // ObjectMapper: JSON 데이터를 Java 객체로 변환하거나, Java 객체를 JSON 데이터로 변환
    // DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false: JSON에 Java 클래스에 정의되지 않은 필드가 있어도 에러를 발생시키지 않도록 설정
    // java.time 패키지의 날짜/시간 클래스(LocalDate, LocalDateTime 등)를 처리할 수 있는 모듈을 추가
    // activateDefaultTyping: Jackson에서 직렬화/역직렬화 시 객체 타입 정보를 포함하도록 설정하는 메서드
    // DefaultTyping.NON_FINAL: 비최종 클래스(Non-final class)에 대해서만 타입 정보를 포함
    // SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS:
    // 날짜를 타임스탬프가 아닌 ISO-8601 포맷(예: 2023-01-18T10:15:30)으로 직렬화하도록 설정

    // Object Mapper 설정 이유
    // Java 객체의 구조에 맞지 않는 JSON 필드가 있을 경우 에러가 발생할 수 있음
    // Java의 날짜/시간 클래스(LocalDate, LocalDateTime 등)는 기본적으로 직렬화/역직렬화가 제대로 지원되지 않음
    // 기본적으로 날짜와 시간을 타임스탬프 형식(예: 1674042600000, 밀리초 단위)으로 직렬화
    // 날짜를 사람이 읽을 수 있는 ISO 형식(예: 2025-01-18T15:30:00)으로 변환


    List<CacheProperty> properties = List.of(
        new CacheProperty(CACHE1, 300),
        new CacheProperty(CACHE2, 30)
    );

    return (builder -> {
      properties.forEach(i ->
          builder.withCacheConfiguration(i.getName(),
              RedisCacheConfiguration.defaultCacheConfig()
              .disableCachingNullValues()
              .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
              .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper)))
              .entryTtl(Duration.ofSeconds(i.getTtl()))));
    });
    // RedisCacheConfiguration.defaultCacheConfig(): 기본 Redis 캐시 설정을 가져옴
    // disableCachingNullValues(): null 값을 캐싱하지 않도록 설정
    // serializeKeysWith: Redis의 키를 직렬화하는 방법을 설정, 키를 문자열로 직렬화
    // serializeValuesWith: Redis의 값을 직렬화하는 방법을 설정, GenericJackson2JsonRedisSerializer를 사용하여 값을 JSON으로 직렬화
    // entryTtl: 캐시의 만료 시간을 설정
  }
}
