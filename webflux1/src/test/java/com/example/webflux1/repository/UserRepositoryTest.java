package com.example.webflux1.repository;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

class UserRepositoryTest {
  private final UserRepository userRepository = new UserRepositoryImpl();

  @Test
  void save() {
    User user = User.builder().name("user1").email("user1@email.com").build();

    StepVerifier.create(userRepository.save(user))
                .assertNext(u -> {
                  assertEquals("user1", u.getName());
                  assertEquals("user1@email.com", u.getEmail());
                })
                .verifyComplete();
  }

  @Test
  void findAll() {
    userRepository.save(User.builder().name("user1").email("user1@email.com").build());
    userRepository.save(User.builder().name("user2").email("user2@email.com").build());
    userRepository.save(User.builder().name("user3").email("user3@email.com").build());

    StepVerifier.create(userRepository.findAll())
                .expectNextCount(3)
                .verifyComplete();
  }

  @Test
  void findById() {
    userRepository.save(User.builder().name("user1").email("user1@email.com").build());
    userRepository.save(User.builder().name("user2").email("user2@email.com").build());

    StepVerifier.create(userRepository.findById(2L))
                .assertNext(u -> {
                  assertEquals("user2", u.getName());
                  assertEquals("user2@email.com", u.getEmail());
                })
        .verifyComplete();
  }

  @Test
  void deleteById() {
    userRepository.save(User.builder().name("user1").email("user1@email.com").build());
    userRepository.save(User.builder().name("user2").email("user2@email.com").build());

    StepVerifier.create(userRepository.deleteById(1L))
                .expectNextCount(1)
                .verifyComplete();
  }
}