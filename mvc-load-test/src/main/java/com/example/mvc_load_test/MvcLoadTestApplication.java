package com.example.mvc_load_test;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@RequiredArgsConstructor
@EnableJpaAuditing
public class MvcLoadTestApplication implements ApplicationListener<ApplicationReadyEvent> {

	private final RedisTemplate<String, String> redisTemplate;
	private final UserRepository userRepository;

	public static void main(String[] args) {
		SpringApplication.run(MvcLoadTestApplication.class, args);
	}

	// case1: DB 요청 X
	@GetMapping("/health")
	public Map<String, String> health() {
		return Map.of("health", "ok");
	}

	// case2: Redis 요청
	@GetMapping("/users/1/cache")
	public Map<String, String> getCachedUser(){
		String name = redisTemplate.opsForValue().get("users:1:name");
		String email = redisTemplate.opsForValue().get("users:1:email");

		return Map.of("name", name == null ? "" : name, "email", email == null ? "" : email);
	}

	// case2: MySQL 요청
	@GetMapping("/users/{id}")
	public User getUser(@PathVariable Long id) {
		return userRepository.findById(id).orElse(new User());
	}

	@Override
	public void onApplicationEvent(ApplicationReadyEvent event) {
		redisTemplate.opsForValue().set("users:1:name", "hello");
		redisTemplate.opsForValue().set("users:1:email", "hello@world.com");

		Optional<User> user = userRepository.findById(1L);
		if (user.isEmpty()) {
			userRepository.save(User.builder().name("hello").email("hello@world.com").build());
		}
	}
}

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
class User {
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Id
	private Long id;
	private String name;
	private String email;
	@CreatedDate
	private LocalDateTime createdAt;
	@LastModifiedDate
	private LocalDateTime updatedAt;
}

interface UserRepository extends JpaRepository<User, Long> {

}