package com.traffic.flow.controller;

import com.traffic.flow.dto.AllowUserResponse;
import com.traffic.flow.dto.AllowedUserResponse;
import com.traffic.flow.dto.RankNumberResponse;
import com.traffic.flow.dto.RegisterUserResponse;
import com.traffic.flow.service.UserQueueService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
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
  public Mono<AllowUserResponse> allowUser(@RequestParam(name = "queue", defaultValue = "default") String queue,
                          @RequestParam(name = "count") Long count) {
    return userQueueService.allowUser(queue, count)
        .map(allowed -> new AllowUserResponse(count, allowed));
  }

  @GetMapping("/allowed")
  public Mono<AllowedUserResponse> isAllowedUser(@RequestParam(name = "queue", defaultValue = "default") String queue,
                                                @RequestParam(name = "user_id") Long userId,
                                                @RequestParam(name = "token")String token) {
    return userQueueService.isAllowedByToken(queue, userId, token)
        .map(AllowedUserResponse::new);
  }

  @GetMapping("/rank")
  public Mono<RankNumberResponse> getRankUser(@RequestParam(name = "queue", defaultValue = "default") String queue,
      @RequestParam(name = "user_id") Long userId) {
    return userQueueService.getRank(queue, userId)
                           .map(RankNumberResponse::new);
  }

  @GetMapping("/touch")
  Mono<String> touch(@RequestParam(name = "queue", defaultValue = "default") String queue,
      @RequestParam(name = "user_id") Long userId,
      ServerWebExchange exchange) {
    // ServerWebExchange: 요청 및 응답을 조작하는 객체 (WebFlux에서 HttpServletRequest 대체)
    return Mono.defer(() -> userQueueService.generateToken(queue, userId))
        .map(token -> {
          exchange.getResponse().addCookie(
              ResponseCookie.from("user-queue-%s-token".formatted(queue), token)
                  .maxAge(Duration.ofSeconds(300))
                  .path("/")
                  .build()
          );
          // defer: 지연 실행 (lazy execution) 을 보장, 클라이언트 요청이 들어올 때까지 generateToken() 실행 안 함

          return token;
        });
  }
}
