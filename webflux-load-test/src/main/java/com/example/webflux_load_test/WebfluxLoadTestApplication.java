package com.example.webflux_load_test;

import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
@RequiredArgsConstructor
public class WebfluxLoadTestApplication {
	private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
	private final UserRepository userRepository;

	public static void main(String[] args) {
		SpringApplication.run(WebfluxLoadTestApplication.class, args);
	}

	// case1: DB 요청 X
	@GetMapping("/health")
	public Map<String, String> health() {
		return Map.of("health", "ok");
	}

	// case2: Redis 요청
	@GetMapping("/users/1/cache")
	public Mono<Map<String, String>> getCachedUser(){
		Mono<String> name = reactiveRedisTemplate.opsForValue().get("users:1:name");
		Mono<String> email = reactiveRedisTemplate.opsForValue().get("users:1:email");

		return Mono.zip(name, email)
				.map(i -> Map.of("name", i.getT1(), "email", i.getT2()));
	}

	// case2: MySQL 요청
	@GetMapping("/users/{id}")
	public Mono<User> getUser(@PathVariable Long id) {
		return userRepository.findById(id).defaultIfEmpty(new User());
	}

}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
class User {
	@Id
	private Long id;
	private String name;
	private String email;
	@CreatedDate
	private LocalDateTime createdAt;
	@LastModifiedDate
	private LocalDateTime updatedAt;
}

interface UserRepository extends ReactiveCrudRepository<User, Long> {

}

