package com.example.webflux1.repository;

import reactor.core.publisher.Flux;

public interface PostR2dbcCustomRepository {
  Flux<Post> findAllByUserId(Long userId);
}
