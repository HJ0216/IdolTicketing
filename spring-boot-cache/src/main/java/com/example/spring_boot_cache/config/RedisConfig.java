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
// Spring의 캐싱 기능을 활성화
// 캐싱 관련 애너테이션(@Cacheable, @CacheEvict, @CachePut)이 동작하도록 설정
// 애플리케이션 컨텍스트에 한 번만 추가되면 전체 프로젝트에서 동작
@Configuration
public class RedisConfig {

  /**
   * Redis에 데이터를 저장하고 가져올 때, User 객체를 쉽게 직렬화/역직렬화하여 처리할 수 있도록 RedisTemplate을 설정
   * */
  @Bean
  RedisTemplate<String, User> userRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
    // RedisConnectionFactory: Redis 서버와의 연결을 관리하는 클래스
    ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
    // ObjectMapper: JSON 데이터를 Java 객체로 변환하거나, Java 객체를 JSON 데이터로 변환
    // DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false: JSON에 Java 클래스에 정의되지 않은 필드가 있어도 에러를 발생시키지 않도록 설정
    // JavaTimeModule: java.time 패키지의 날짜/시간 클래스(LocalDate, LocalDateTime 등)를 처리할 수 있는 모듈을 추가
    // SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS
    // : 날짜를 타임스탬프가 아닌 ISO-8601 포맷(예: 2023-01-18T10:15:30)으로 직렬화하도록 설정

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
    // PolymorphicTypeValidator: allowIfSubType을 통해 역직렬화 허용 클래스 타입을 정의

    ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(new JavaTimeModule())
        .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL)
        .disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
    // activateDefaultTyping: Jackson에서 직렬화/역직렬화 시 객체 타입 정보를 포함하도록 설정하는 메서드
    // DefaultTyping: 비최종 클래스(Non-final class)에 대해서만 타입 정보를 포함

    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
    // RedisTemplate를 광범위하게 사용할 수 있도록 Value를 Object로 확장
    // GenericJackson2JsonRedisSerializer
    // : JSON 데이터를 역직렬화할 때, 대상 객체의 타입 정보를 명시적으로 제공하지 않으면 기본적으로 Map 타입(즉, LinkedHashMap)으로 역직렬화

    return template;
  }


}
