package com.example.webflux1.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.example.webflux1.dto.UserCreateRequest;
import com.example.webflux1.dto.UserResponse;
import com.example.webflux1.repository.User;
import com.example.webflux1.service.PostR2dbcService;
import com.example.webflux1.service.UserService;
import java.time.Duration;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.blockhound.BlockHound;
import reactor.blockhound.BlockingOperationError;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@WebFluxTest(controllers = UserController.class)
@AutoConfigureWebTestClient
class UserControllerTest {
  static {
      BlockHound.install(builder ->
          builder.allowBlockingCallsInside(UserControllerTest.class.getName(), "allowedBlockingMethod")
      );
  }

  @Autowired
  private WebTestClient client;

  // MockBean
  // private UserService userService

  @MockitoBean // 가짜 객체(Mock) 생성
  private UserService userService;

  @MockitoBean // 가짜 객체(Mock) 생성
  private PostR2dbcService postR2dbcService;

  @Test
  void shouldDetectBlockingCall() {
    Mono<Long> blockingMono = Mono.delay(Duration.ofSeconds(1))
                                  .doOnNext(t -> {
                                    try {
                                      Thread.sleep(100);  // 블로킹 호출
                                    } catch (InterruptedException e) {
                                      throw new RuntimeException(e);
                                    }
                                  });

    StepVerifier.create(blockingMono)
                .expectError(BlockingOperationError.class) // 에러 타입을 검증
                .verify();
  }

  // 1️⃣ 블로킹 호출을 별도 메서드로 분리
  void allowedBlockingMethod() {
    try {
      Thread.sleep(100);  // 블로킹 호출
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  // 2️⃣ 분리한 메서드를 호출
  @Test
  void shouldAllowBlockingCallInsideAllowedMethod() {
    Mono<Long> allowedMono = Mono.delay(Duration.ofSeconds(1))
                                     .doOnNext(__ -> allowedBlockingMethod());

    StepVerifier.create(allowedMono)
                .expectNextCount(1) // Mono.delay(Duration.ofSeconds(1))는 1초 후에 0L이라는 값을 방출
                .verifyComplete();
  }

  @Test
  void createUser() {
    when(userService.create("malrangcow", "malrangcow@gmail.com")).thenReturn(
        Mono.just(new User(1L, "malrangcow", "malrangcow@gmail.com", LocalDateTime.now(), LocalDateTime.now()))
    );

    client.post().uri("/users")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(new UserCreateRequest("malrangcow", "malrangcow@gmail.com"))
          .exchange() // 실제로 요청을 실행 (비동기 방식)
          .expectStatus().is2xxSuccessful()
          .expectBody(UserResponse.class)
          .value(res -> {
            assertEquals("malrangcow", res.getName());
            assertEquals("malrangcow@gmail.com", res.getEmail());
          });
  }

  @Test
  void findAllUsers() {
    when(userService.findAll()).thenReturn(
        Flux.just(
            new User(1L, "malrang1cow", "malrang1cow@gmail.com", LocalDateTime.now(), LocalDateTime.now()),
            new User(2L, "malrang2cow", "malrang2cow@gmail.com", LocalDateTime.now(), LocalDateTime.now()),
            new User(3L, "malrang3cow", "malrang3cow@gmail.com", LocalDateTime.now(), LocalDateTime.now())
        )
    );

    client.get().uri("/users")
          .exchange()
          .expectStatus().is2xxSuccessful()
          .expectBodyList(UserResponse.class)
          .hasSize(3);
  }

  @Test
  void findUserById() {
    when(userService.findById(1L)).thenReturn(
        Mono.just(
            new User(1L, "malrang1cow", "malrang1cow@gmail.com", LocalDateTime.now(), LocalDateTime.now())
        )
    );

    client.get().uri("/users/1")
          .exchange()
          .expectStatus().is2xxSuccessful()
          .expectBody(UserResponse.class)
        .value(res -> {
          assertEquals("malrang1cow", res.getName());
          assertEquals("malrang1cow@gmail.com", res.getEmail());
        });
  }

  @Test
  void notFoundUserById() {
    when(userService.findById(1L)).thenReturn(
        Mono.empty()
    );

    client.get().uri("/users/1")
          .exchange()
          .expectStatus().is4xxClientError();
  }

  @Test
  void updateUser() {
    when(userService.update(1L,"malrang1cow", "malrang1cow@gmail.com")).thenReturn(
        Mono.just(new User(1L, "malrang1cow", "malrang1cow@gmail.com", LocalDateTime.now(), LocalDateTime.now()))
    );

    client.put().uri("/users/1")
          .contentType(MediaType.APPLICATION_JSON)
          .bodyValue(new UserCreateRequest("malrang1cow", "malrang1cow@gmail.com"))
          .exchange() // 실제로 요청을 실행 (비동기 방식)
          .expectStatus().is2xxSuccessful()
          .expectBody(UserResponse.class)
          .value(res -> {
            assertEquals("malrang1cow", res.getName());
            assertEquals("malrang1cow@gmail.com", res.getEmail());
          });
  }

//  @Test
//  @DisplayName("ConcurrentHashMap")
//  void deleteUserById() {
//    when(userService.deleteById(1L)).thenReturn(
//        Mono.just(1)
//    );
//
//    client.delete().uri("/users/1")
//          .exchange() // 실제로 요청을 실행 (비동기 방식)
//          .expectStatus().is2xxSuccessful();
//  }

  @Test
  @DisplayName("R2DBC")
  void deleteUserById() {
    when(userService.findById(1L)).thenReturn(Mono.just(new User()));
    when(userService.deleteById(1L)).thenReturn(
        Mono.empty()
    );

    client.delete().uri("/users/1")
          .exchange() // 실제로 요청을 실행 (비동기 방식)
          .expectStatus().is2xxSuccessful();
  }
}