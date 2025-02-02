package com.example.webflux1.config;

import com.example.webflux1.repository.User;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class RedisConfig implements ApplicationListener<ApplicationReadyEvent> {
  private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    reactiveRedisTemplate.opsForValue().get("1")
        .doOnSuccess(i -> log.info("Initialize to redis connection"))
        .doOnError(e -> log.error("Fail to initializing Redis connection", e.getMessage()))
        .subscribe();
  }

  // ReactiveRedisTemplate<String, String> 제외하고는 빈 수동 등록
  // 안할 경우, a bean of type 'org.springframework.data.redis.core.ReactiveRedisTemplate' that could not be found 오류 발생
  @Bean
  public ReactiveRedisTemplate<String, User> reactiveRedisUserTemplate(
      ReactiveRedisConnectionFactory connectionFactory) {
    ObjectMapper objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);

    Jackson2JsonRedisSerializer<User> jsonSerializer = new Jackson2JsonRedisSerializer<>(
        objectMapper, User.class);

    RedisSerializationContext<String, User> serializationContext = RedisSerializationContext
        .<String, User>newSerializationContext() // 직렬화 컨텍스트를 만들기 위한 빌더를 초기화
        .key(RedisSerializer.string()) // 문자열을 UTF-8 인코딩으로 직렬화/역직렬화
        .value(jsonSerializer) // User 객체를 JSON 형식으로 직렬화
        .hashKey(RedisSerializer.string()) // Redis의 Hash 자료구조를 사용할 경우, 내부 필드의 Key 직렬화 방식입
        .hashValue(jsonSerializer) // Hash 자료구조 내부의 Value를 JSON으로 직렬화
        .build();
    // Spring Data Redis (Reactive) 환경에서 Redis에 데이터를 저장하거나 조회할 때, Key와 Value를 어떻게 직렬화(Serialization)하고 역직렬화(Deserialization)할지 정의
    // Hash 자료 구조를 따로 직렬화/역직렬화 설정해야 하는 이유: 자료구조가 상이
    // hset user:1 id 1
    // hset user:1 name "Alice"
    // id, name → Hash Key
    // 1, "Alice" → Hash Value

    return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);

  }

}
