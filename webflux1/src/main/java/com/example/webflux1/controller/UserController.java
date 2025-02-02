package com.example.webflux1.controller;

import com.example.webflux1.dto.UserCreateRequest;
import com.example.webflux1.dto.UserPostResponse;
import com.example.webflux1.dto.UserResponse;
import com.example.webflux1.dto.UserUpdateRequest;
import com.example.webflux1.service.PostR2dbcService;
import com.example.webflux1.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

  private final UserService userService;
  private final PostR2dbcService postR2dbcService;

  @PostMapping("")
  public Mono<UserResponse> createUser(@RequestBody UserCreateRequest request) {
    return userService.create(request.getName(), request.getEmail())
                      .map(UserResponse::of);
  }

/*
  @GetMapping("")
  public Flux<ResponseEntity<UserResponse>> findAllUsers() {
    return userService.findAll()
                      .map(u -> ResponseEntity.ok(UserResponse.of(u)))
                      .switchIfEmpty(Flux.just(ResponseEntity.notFound().build()));
  }
*/

  @GetMapping("")
  public Mono<ResponseEntity<Flux<UserResponse>>> findAllUsers() {
    return userService.findAll()
                      .map(UserResponse::of) // User -> UserResponse로 변환
                      .collectList() // Flux를 List로 변환
                      .flatMap(users -> {
                        if (users.isEmpty()) {
                          return Mono.just(ResponseEntity.notFound().build());
                        }
                        return Mono.just(ResponseEntity.ok(Flux.fromIterable(users)));
                      });
  }

  @GetMapping("/{id}")
  public Mono<ResponseEntity<UserResponse>> findUserById(@PathVariable Long id) {
    return userService.findById(id)
                      .map(u -> ResponseEntity.ok(UserResponse.of(u)))
                      .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
  }

  @GetMapping("/{id}/posts")
  public Flux<UserPostResponse> getUserPosts(@PathVariable Long id) {
    return postR2dbcService.findAllByUserId(id)
        .map(UserPostResponse::of);
  }

  @PutMapping("/{id}")
  public Mono<ResponseEntity<UserResponse>> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request){
    // user x: 404, Not Found
    // user o: 200, OK
    return userService.update(id, request.getName(), request.getEmail())
                      .map(u -> ResponseEntity.ok(UserResponse.of(u)))
                      .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
  }

//  @DeleteMapping("/{id}")
//  public Mono<ResponseEntity<Object>> deleteUserById(@PathVariable Long id) {
//    // user x: 404, Not Found
//    // user o: 204, No Content
//    return userService.deleteById(id)
//                          .flatMap(deleteCount -> {
//                            if (deleteCount == 0) {
//                              return Mono.just(ResponseEntity.notFound().build());
//                            }
//                            return Mono.just(ResponseEntity.noContent().build());
//                          });
//  }
  @DeleteMapping("/{id}")
  public Mono<ResponseEntity<Object>> deleteUserById(@PathVariable Long id) {
    // user x: 404, Not Found
    // user o: 204, No Content
    return userService.findById(id)
                      .flatMap(user -> userService.deleteById(id)
                                                  .then(
                                                      Mono.just(ResponseEntity.noContent().build()))
                      )
                      .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));

    // findById(id)가 비어 있으면(Mono.empty()), switchIfEmpty()가 실행되어 404 Not Found를 반환
    // Mono<T>는 값이 존재하면 .flatMap()을 실행하지만, 값이 없으면 건너 뜀
  }

  @DeleteMapping("/search")
  public Mono<ResponseEntity<Void>> deleteUserByName(@RequestParam String name) {
    // user x: 404, Not Found
    // user o: 204, No Content
    return userService.findByName(name)
                      .collectList()  // Flux<User> → Mono<List<User>>
                      .flatMap(users -> {
                        long expectedDeleteCount = users.size();

                        if (expectedDeleteCount == 0) {
                          return Mono.just(ResponseEntity.notFound().build());
                        }

                        return userService.deleteByName(name)
                                          .flatMap(deletedCount ->
                                              deletedCount < expectedDeleteCount  // 하나라도 삭제 실패?
                                                  ? Mono.just(ResponseEntity.notFound().build())
                                                  : Mono.just(ResponseEntity.noContent().build())
                                          );
                      });
  }

}
