package com.traffic.flow.service;

import com.traffic.flow.exception.ErrorCode;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserQueueService {
  private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

  // 중복된 String이 자주 사용될 경우, 상수 활용
  private final String USER_QUEUE_WAIT_KEY = "users:queue:%s:wait";
  // %s: queue를 여러 개 운용할 경우에 대비

  // 대기열 등록 API
  public Mono<Long> registerWaitQueue(final String queue, final Long userId) {
    // redis sortedSet
    // - key: userId
    // - value: unix timestamp
    long unixTimeStamp = Instant.now().getEpochSecond();
    // getEpochSecond: 1970년 1월 1일 00:00:00 UTC(Unix epoch)부터 현재까지의 시간을 초(second) 단위로 반환
    return reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString(), unixTimeStamp)
        .filter(isAdded -> isAdded) // 이미 등록된 회원은 add에서 false 반환 -> switchIfEmpty로 넘어감
        .switchIfEmpty(Mono.error(ErrorCode.QUEUE_ALREADY_REGISTERED_USER.build()))
        .flatMap(isAdded -> reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString()))
        .map(rank -> rank >= 0 ? rank + 1 : rank); // 등록된 회원은 true를 반환
  }

}
