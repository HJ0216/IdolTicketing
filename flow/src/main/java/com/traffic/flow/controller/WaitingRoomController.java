package com.traffic.flow.controller;

import com.traffic.flow.service.UserQueueService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

@Controller
public class WaitingRoomController {

  private final UserQueueService userQueueService;

  public WaitingRoomController(UserQueueService userQueueService) {
    this.userQueueService = userQueueService;
  }

  @GetMapping("/waiting-room")
  Mono<Rendering> waitingRoomPage(
      @RequestParam(name = "queue", defaultValue = "default") String queue,
      @RequestParam(name = "user_id") Long userId,
      @RequestParam(name = "redirect_url") String redirectUrl) {
    // 1. 입장이 허용되어 page redirect가 가능한 상태인가
    // 2. 어디로 이동해야하는가
    return userQueueService.isAllowed(queue, userId)
        .filter(allowed -> allowed)
        .flatMap(allowed -> Mono.just(Rendering.redirectTo(redirectUrl).build()))
        .switchIfEmpty(
            // 대기 등록
            // 웹페이지에 필요한 데이터 전달
            userQueueService.registerWaitQueue(queue, userId)
                            .onErrorResume(ex -> userQueueService.getRank(queue, userId))
                            .map(rank -> Rendering.view("waiting-room.html")
                                                  .modelAttribute("number", rank)
                                                  .modelAttribute("userId", userId)
                                                  .modelAttribute("queue", queue)
                                                  .build())
        );
  }
}
