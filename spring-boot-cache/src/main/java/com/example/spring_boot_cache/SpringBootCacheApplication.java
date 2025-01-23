package com.example.spring_boot_cache;

import com.example.spring_boot_cache.domain.entity.User;
import com.example.spring_boot_cache.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@EnableJpaAuditing
@SpringBootApplication
@RequiredArgsConstructor
public class SpringBootCacheApplication implements ApplicationRunner {
	private final UserRepository userRepository;

	public static void main(String[] args) {
		SpringApplication.run(SpringBootCacheApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		userRepository.save(User.builder().name("hi1").email("hi1@email.com").build());
		userRepository.save(User.builder().name("hi2").email("hi2@email.com").build());
		userRepository.save(User.builder().name("hi3").email("hi3@email.com").build());
	}

	@GetMapping(value = "/")
	String home(){
		return "OK";
	}
}
