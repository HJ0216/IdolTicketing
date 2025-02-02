package com.example.webflux1.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserR2dbcRepository extends ReactiveCrudRepository<User, Long> {

  Flux<User> findByName(String name);
  Flux<User> findByNameOrderByIdDesc(String name);

  @Modifying
  // @Query: 읽기 전용(SELECT 쿼리)으로 동작
  // CREATE, UPDATE, DELETE 같은 데이터를 변경하는 쿼리를 실행하려면 @Modifying을 추가
  @Query("DELETE FROM users WHERE name = :name")
  Mono<Integer> deleteByName(String name);
}
