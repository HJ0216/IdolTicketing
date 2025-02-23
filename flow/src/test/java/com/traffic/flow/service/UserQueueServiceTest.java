package com.traffic.flow.service;

import static org.junit.jupiter.api.Assertions.*;

import com.traffic.flow.EmbeddedRedis;
import com.traffic.flow.exception.ApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnection;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

@SpringBootTest
// 테스트를 실행할 때도 Spring 컨텍스트(ApplicationContext)를 로드해야 빈을 사용할 수 있음
//  @SpringBootTest 덕분에 Spring 컨텍스트가 로드되고, Service가 자동으로 빈으로 등록되어 @Autowired가 정상적으로 동작
@Import(EmbeddedRedis.class)
@ActiveProfiles("test")
class UserQueueServiceTest {

  @Autowired
  private UserQueueService userQueueService;

  @Autowired
  private ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
  
  // test를 독립적으로 수행하기 위해 데이터 정리
  @BeforeEach
  public void beforeEach(){
    ReactiveRedisConnection connection = reactiveRedisTemplate.getConnectionFactory()
                                                                      .getReactiveConnection();
    connection.serverCommands().flushAll().subscribe();
  }

  @Test
  void registerWaitQueue() {
    StepVerifier.create(userQueueService.registerWaitQueue("default", 100L))
        .expectNext(1L)
        .verifyComplete();
    StepVerifier.create(userQueueService.registerWaitQueue("default", 101L))
        .expectNext(2L)
        .verifyComplete();
    StepVerifier.create(userQueueService.registerWaitQueue("default", 102L))
        .expectNext(3L)
        .verifyComplete();
  }

  @Test
  void alreadyRegisterWaitQueue() {
    StepVerifier.create(userQueueService.registerWaitQueue("default", 100L))
        .expectNext(1L)
        .verifyComplete();

    StepVerifier.create(userQueueService.registerWaitQueue("default", 100L))
        .expectError(ApplicationException.class)
        .verify();
  }

  @Test
  void emptyAllowUser(){
    StepVerifier.create(userQueueService.allowUser("default", 3L))
        .expectNext(0L)
        .verifyComplete();
  }

  @Test
  void allowUser() {
    StepVerifier.create(userQueueService.registerWaitQueue("default", 100L)
        .then(userQueueService.registerWaitQueue("default", 101L))
        .then(userQueueService.registerWaitQueue("default", 102L))
        .then(userQueueService.allowUser("default", 2L)))
        .expectNext(2L)
        .verifyComplete();
  }

  @Test
  void allowUser2() {
    StepVerifier.create(userQueueService.registerWaitQueue("default", 100L)
                                        .then(userQueueService.registerWaitQueue("default", 101L))
                                        .then(userQueueService.registerWaitQueue("default", 102L))
                                        .then(userQueueService.allowUser("default", 4L)))
                .expectNext(3L)
                .verifyComplete();
  }

  @Test
  void registerWaitQueueAfterAllowUser() {
    StepVerifier.create(userQueueService.registerWaitQueue("default", 100L)
                                        .then(userQueueService.registerWaitQueue("default", 101L))
                                        .then(userQueueService.registerWaitQueue("default", 102L))
                                        .then(userQueueService.allowUser("default", 4L))
                    .then(userQueueService.registerWaitQueue("default", 200L)))
                .expectNext(1L)
                .verifyComplete();

  }

  @Test
  void isNotAllowed() {
    StepVerifier.create(userQueueService.isAllowed("default", 100L))
                .expectNext(false)
                .verifyComplete();
  }

  @Test
  void isNotAllowed2() {
    StepVerifier.create(userQueueService.registerWaitQueue("default", 100L)
                                        .then(userQueueService.allowUser("default", 3L))
                                        .then(userQueueService.isAllowed("default", 200L)))
                .expectNext(false)
                .verifyComplete();
  }

  @Test
  void isAllowed() {
    StepVerifier.create(userQueueService.registerWaitQueue("default", 100L)
                                        .then(userQueueService.allowUser("default", 3L))
                                        .then(userQueueService.isAllowed("default", 100L)))
                .expectNext(true)
                .verifyComplete();
  }

  @Test
  void getRank(){
    StepVerifier.create(userQueueService.registerWaitQueue("default", 100L)
                                        .then(userQueueService.getRank("default", 100L)))
                .expectNext(1L)
                .verifyComplete();
  }

  @Test
  void getEmptyRank(){
    StepVerifier.create(userQueueService.getRank("default", 100L))
                .expectNext(-1L)
                .verifyComplete();
  }

  @Test
  void IsNotAllowedByToken() {
    StepVerifier.create(userQueueService.isAllowedByToken("default", 100L, ""))
                .expectNext(false)
                .verifyComplete();
  }

  @Test
  void IsAllowedByToken() {
    StepVerifier.create(userQueueService.isAllowedByToken("default", 100L, "d333a5d4eb24f3f5cdd767d79b8c01aad3cd73d3537c70dec430455d37afe4b8"))
                .expectNext(true)
                .verifyComplete();
  }

  @Test
  void generateToken() {
    StepVerifier.create(userQueueService.generateToken("default", 100L))
                .expectNext("d333a5d4eb24f3f5cdd767d79b8c01aad3cd73d3537c70dec430455d37afe4b8")
                .verifyComplete();
  }
}