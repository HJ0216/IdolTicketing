package com.traffic.flow.controller;

import com.traffic.flow.dto.AllowUserResponse;
import com.traffic.flow.dto.AllowedUserResponse;
import com.traffic.flow.dto.RegisterUserResponse;
import com.traffic.flow.service.UserQueueService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/queue")
@RequiredArgsConstructor
public class UserQueueController {

  private final UserQueueService userQueueService;

  @PostMapping("")
  public Mono<RegisterUserResponse> registerUser(
      @RequestParam(name = "queue", defaultValue = "default") String queue,
      @RequestParam(name = "user_id") Long userId) {
    return userQueueService.registerWaitQueue(queue, userId)
                           .map(RegisterUserResponse::new);
    // Spring WebFlux는 Jackson을 이용해 record를 자동 직렬화(JSON 변환)함
    // record는 자동으로 getter가 생성
    // Jackson이 getter 메서드를 JSON 필드로 변환
  }

  @PostMapping("/allow")
  public Mono<?> allowUser(@RequestParam(name = "queue", defaultValue = "default") String queue,
                          @RequestParam(name = "count") Long count) {
    return userQueueService.allowUser(queue, count)
        .map(allowed -> new AllowUserResponse(count, allowed));
  }

  @GetMapping("/allowed")
  public Mono<AllowedUserResponse> isAllowedUser(@RequestParam(name = "queue", defaultValue = "default") String queue,
                                                @RequestParam(name = "user_id") Long userId) {
    return userQueueService.isAllowed(queue, userId)
        .map(AllowedUserResponse::new);
  }
}
