package com.example.spring_boot_cache.domain.entity;

import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@RedisHash(value = "redishash-user", timeToLive = 30L)
// @RedisHash를 사용해 Redis에 저장할 객체를 정의
public class RedisHashUser {

  @Id
  private Long id;
  // HSET redishash-user:3 id 3
  private String name;
  // HSET redishash-user:3 name "hi3"
  @Indexed
  private String email;
  // HSET redishash-user:3 email "hi3@email.com"
  // "SADD" "redishash-user:email:hi3@email.com" 3
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}
