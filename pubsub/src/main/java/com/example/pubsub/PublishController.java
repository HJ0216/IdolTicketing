package com.example.pubsub;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PublishController {

  private final RedisTemplate<String, String> redisTemplate;
  // Spring이 제공하는 Redis와 통신하기 위한 도구
  // String 타입의 채널 이름과 메시지를 Redis로 보낼 수 있게 설정

  @PostMapping("/events/users/deregister")
  void publishUserDeregisterEvent() {
    redisTemplate.convertAndSend("users:unregister", "234");
    // RedisTemplate이 users:unregister 채널에 메시지 "234"를 전송
  }
}
