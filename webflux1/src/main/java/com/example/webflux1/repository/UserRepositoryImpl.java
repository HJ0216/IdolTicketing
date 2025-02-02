package com.example.webflux1.repository;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class UserRepositoryImpl implements UserRepository {
  private final ConcurrentHashMap<Long, User> userHashMap = new ConcurrentHashMap<Long, User>();
  private AtomicLong sequence = new AtomicLong(1L);

  @Override
  public Mono<User> save(User user) {
    // created_at, updated_at
    LocalDateTime now = LocalDateTime.now();

    if (user.getId() == null) {
      user.setId(sequence.getAndAdd(1));
      user.setCreatedAt(now);
    }

    user.setUpdatedAt(now);
    userHashMap.put(user.getId(), user);
    // concurrentMap은 put 사용 시, 값 교체로 진행됨
    return Mono.just(user);
  }

  @Override
  public Flux<User> findAll() {
    return Flux.fromIterable(userHashMap.values());
  }

  @Override
  public Mono<User> findById(Long id) {
    return Mono.justOrEmpty(userHashMap.getOrDefault(id, null));
  }

  @Override
  public Mono<Integer> deleteById(Long id) {
    User user = userHashMap.getOrDefault(id, null);
    if (user == null) {
      return Mono.just(0);
    }

    userHashMap.remove(id, user); // 키 & 값 둘 다 일치해야 삭제
    return Mono.just(1);
  }
}
