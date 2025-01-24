package com.example.spring_boot_cache.config;

import com.example.spring_boot_cache.domain.entity.User;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator.Builder;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@EnableCaching
@Configuration
public class RedisConfig {

  /**
   * Redis에 데이터를 저장하고 가져올 때, User 객체를 쉽게 직렬화/역직렬화하여 처리할 수 있도록 RedisTemplate을 설정
   * */
  @Bean
  RedisTemplate<String, User> userRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
    // RedisConnectionFactory: Redis 서버와의 연결을 관리하는 클래스
    ObjectMapper objectMapper = new ObjectMapper().configure(
                                                 DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                             .registerModule(new JavaTimeModule())
                                             .disable(
                                                 SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
    // ObjectMapper: JSON 데이터를 Java 객체로 변환하거나, Java 객체를 JSON 데이터로 변환
    // DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false: JSON에 Java 클래스에 정의되지 않은 필드가 있어도 에러를 발생시키지 않도록 설정
    // java.time 패키지의 날짜/시간 클래스(LocalDate, LocalDateTime 등)를 처리할 수 있는 모듈을 추가
    // SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS:
    // 날짜를 타임스탬프가 아닌 ISO-8601 포맷(예: 2023-01-18T10:15:30)으로 직렬화하도록 설정

    // Object Mapper 설정 이유
    // Java 객체의 구조에 맞지 않는 JSON 필드가 있을 경우 에러가 발생할 수 있음
    // Java의 날짜/시간 클래스(LocalDate, LocalDateTime 등)는 기본적으로 직렬화/역직렬화가 제대로 지원되지 않음
    // 기본적으로 날짜와 시간을 타임스탬프 형식(예: 1674042600000, 밀리초 단위)으로 직렬화
    // 날짜를 사람이 읽을 수 있는 ISO 형식(예: 2025-01-18T15:30:00)으로 변환

    RedisTemplate<String, User> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory);
    template.setKeySerializer(new StringRedisSerializer()); // 키를 문자열(String)로 직렬화
    template.setValueSerializer(new Jackson2JsonRedisSerializer<>(objectMapper, User.class)); // JSON 포맷으로 데이터를 저장

    return template;
  }

  @Bean
  RedisTemplate<String, Object> objectRedisTemplate(RedisConnectionFactory connectionFactory) {
    PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator
        .builder()
        .allowIfSubType(Object.class)
        .build();

    ObjectMapper objectMapper = new ObjectMapper().configure(
                                                      DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                                                  .registerModule(new JavaTimeModule())
        .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL)
                                                  .disable(
                                                      SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);

    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
    // RedisTemplate를 광범위하게 사용할 수 있도록 Value를 Object로 확장

    return template;
  }


}
