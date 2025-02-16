package com.traffic.flow.service;

import com.traffic.flow.exception.ErrorCode;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserQueueService {
  private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

  // 중복된 String이 자주 사용될 경우, 상수 활용
  private final String USER_QUEUE_WAIT_KEY = "users:queue:%s:wait";
  // 대기 큐 여러개 설정 가능 -> 운영 중인 대기열을 모두 찾아서 허용
  private final String USER_QUEUE_WAIT_KEY_FOR_SCAN = "users:queue:*:wait";
  // %s: queue를 여러 개 운용할 경우에 대비
  private final String USER_QUEUE_PROCEED_KEY = "users:queue:%s:proceed";

  @Value("${scheduler.enabled}")
  private Boolean scheduling = false;

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

  // 진입 허용 API
  public Mono<Long> allowUser(final String queue, final Long count) {
    // 1. wait queue에서 사용자 제거
    // 2. proceed queue에 사용자 추가
    return reactiveRedisTemplate.opsForZSet().popMin(USER_QUEUE_WAIT_KEY.formatted(queue), count)
        .flatMap(user -> reactiveRedisTemplate.opsForZSet().add(USER_QUEUE_PROCEED_KEY.formatted(queue), user.getValue(), Instant.now().getEpochSecond()))
        .count();
  }

  // 진입 가능 여부 조회 API
  public Mono<Boolean> isAllowed(final String queue, final Long userId) {
    return reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_PROCEED_KEY.formatted(queue), userId.toString())
                                .defaultIfEmpty(-1L)
                                .map(rank -> rank >= 0);
  }

  // 대기번호 조회 API
  public Mono<Long> getRank(final String queue, final Long userId) {
    return reactiveRedisTemplate.opsForZSet().rank(USER_QUEUE_WAIT_KEY.formatted(queue), userId.toString())
        .defaultIfEmpty(-1L)
        .map(rank -> rank >= 0 ? rank + 1 : rank);
  }

  // 특정 주기로 메서드 실행
  @Scheduled(initialDelay = 5000, fixedDelay = 10000) // 서버 시작 후 5초 뒤, 이후 10초마다 메서드 실행
  public void scheduleAllowUser(){
    if(!scheduling){
      log.info("passed scheduling...");
      return;
    }

    log.info("called Scheduling...");

    long maxAllowUserCount = 3L;

    // 사용자를 허용하는 코드 작성
    reactiveRedisTemplate.scan(ScanOptions.scanOptions()
        .match(USER_QUEUE_WAIT_KEY_FOR_SCAN)
        .count(100)
        .build())
        .map(key -> key.split(":")[2])
        .flatMap(queue -> allowUser(queue, maxAllowUserCount).map(allowed -> Tuples.of(queue, allowed)))
        .doOnNext(tuple -> log.info("Tried %d and allowed %d users of %s queue".formatted(maxAllowUserCount, tuple.getT2(), tuple.getT1())))
        .subscribe();
  }

}
