package com.traffic.flow;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

@TestConfiguration
public class EmbeddedRedis {
  private final RedisServer redisServer;

  public EmbeddedRedis() throws IOException {
    this.redisServer = new RedisServer(63790);
  }

  @PostConstruct // EmbeddedRedis 생성 후
  public void start() throws IOException {
    this.redisServer.start();
  }

  @PreDestroy // EmbeddedRedis 소멸 전
  public void stop() throws IOException {
    this.redisServer.stop();
  }

}
