package com.example.webflux1.service;

import com.example.webflux1.repository.User;
import com.example.webflux1.repository.UserR2dbcRepository;
import com.example.webflux1.repository.UserRepository;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {
//  private final UserRepository userRepository;
  private final UserR2dbcRepository userR2dbcRepository;
  private final ReactiveRedisTemplate<String, User> reactiveRedisTemplate;

  public Mono<User> create(String name, String email) {
//    return userRepository.save(User.builder().name(name).email(email).build());
    return userR2dbcRepository.save(User.builder().name(name).email(email).build());
  }

  public Flux<User> findAll() {
//    return userRepository.findAll();
    return userR2dbcRepository.findAll();
  }

  public Mono<User> findById(Long id) {
//    return userRepository.findById(id);
//    return userR2dbcRepository.findById(id);

    return reactiveRedisTemplate
        .opsForValue()
        .get(getUserCacheKey(id))
        .switchIfEmpty(userR2dbcRepository
            .findById(id)
            .flatMap(u -> reactiveRedisTemplate
                .opsForValue()
                .set(getUserCacheKey(id), u, Duration.ofSeconds(30))
                .then(Mono.just(u))));
  }

  private String getUserCacheKey(Long id) {
    return "users:%d".formatted(id);
  }

  public Flux<User> findByName(String name) {
    return userR2dbcRepository.findByName(name);
  }

  public Mono<User> update(Long id, String name, String email) {
//    return userRepository.findById(id)
//        .flatMap(u -> {
//          u.setName(name);
//          u.setEmail(email);
//          return userRepository.save(u);
//        });
//    return userR2dbcRepository.findById(id)
//        .flatMap(u -> {
//          u.setName(name);
//          u.setEmail(email);
//          return userR2dbcRepository.save(u);
//        });

    // u: findById(id)로 조회된 User 객체
    // flatMap 사용 이유: userRepository.save(u)가 Mono<User> 반환 -> map을 쓰면 Mono<Mono<User>>

    return userR2dbcRepository.findById(id)
                              .flatMap(u -> {
                                u.setName(name);
                                u.setEmail(email);
                                return userR2dbcRepository.save(u);
                              })
        .flatMap(u -> reactiveRedisTemplate
            .unlink(getUserCacheKey(id))
            .then(Mono.just(u)));
    
    // update 후, 캐시 삭제 로직 추가
    // unlink: 비동기식 삭제
    // delete: 동기식 삭제

  }

  public Mono<Void> deleteById(Long id) {
//    return userR2dbcRepository.deleteById(id);
    return userR2dbcRepository.deleteById(id)
        .then(reactiveRedisTemplate.unlink(getUserCacheKey(id)))
        .then(Mono.empty());

    // delete 후, 캐시 삭제 로직 추가
  }

  public Mono<Integer> deleteByName(String name) {
    return userR2dbcRepository.deleteByName(name);
  }

}
