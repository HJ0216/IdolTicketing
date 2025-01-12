package com.example.jediscache;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class JediscacheApplication implements ApplicationRunner {
	private final UserRepository userRepository;

	public static void main(String[] args) {
		SpringApplication.run(JediscacheApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		userRepository.save(User.builder().name("mond").email("mond@email.com").build());
		userRepository.save(User.builder().name("tued").email("tued@email.com").build());
		userRepository.save(User.builder().name("wedd").email("wedd@email.com").build());
		userRepository.save(User.builder().name("frid").email("frid@email.com").build());
	}
}
